package AttractorSchool.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BasicServer {

    private final Configuration freemarker;
    private final HttpServer server;
    // путь к каталогу с файлами, которые будет отдавать сервер по запросам клиентов
    private final String dataDir = "data";
    private Map<String, RouteHandler> routes = new HashMap<>();

    protected BasicServer(String host, int port) throws IOException {
        server = createServer(host, port);
        registerCommonHandlers();
        freemarker = initFreeMarker();
    }

    private static String makeKey(String method, String route) {
        route = ensureStartWithSlash(route);
        return String.format("%s %s", method.toUpperCase(), route);
    }

    private static String ensureStartWithSlash(String route) {
        if (route.startsWith(".")) return route;

        return route.startsWith("/") ? route : "/" + route;
    }

    private static String makeKey(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf(".");
        String extOrPath = index != -1 ? path.substring(index).toLowerCase() : path;

        return makeKey(method, extOrPath);
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            // путь к каталогу в котором у нас хранятся шаблоны
            // это может быть совершенно другой путь, чем тот, откуда сервер берёт файлы
            // которые отправляет пользователю
            cfg.setDirectoryForTemplateLoading(new File("data"));

            // прочие стандартные настройки о них читать тут
            // https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            // Загружаем шаблон из файла по имени.
            // Шаблон должен находится по пути, указанном в конфигурации
            Template temp = freemarker.getTemplate(templateFile);

            // freemarker записывает преобразованный шаблон в объект класса writer
            // а наш сервер отправляет клиенту массивы байт
            // по этому нам надо сделать "мост" между этими двумя системами

            // создаём поток, который сохраняет всё, что в него будет записано в байтовый массив
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // создаём объект, который умеет писать в поток и который подходит для freemarker
            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                // обрабатываем шаблон заполняя его данными из модели
                // и записываем результат в объект "записи"
                temp.process(dataModel, writer);
                writer.flush();

                // получаем байтовый поток
                var data = stream.toByteArray();

                // отправляем результат клиенту
                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private static void setContentType(HttpExchange exchange, ContentType type) {
        exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
    }

    private static HttpServer createServer(String host, int port) throws IOException {
        var msg = "Starting server on http://%s:%s/%n";
        System.out.printf(msg, host, port);
        var address = new InetSocketAddress(host, port);
        return HttpServer.create(address, 50);
    }

    private void registerCommonHandlers() {
        // самый основной обработчик, который будет определять
        // какие обработчики вызывать в дальнейшем
        server.createContext("/", this::handleIncomingServerRequests);

        // специфичные обработчики, которые выполняют свои действия
        // в зависимости от типа запроса

        // обработчик для корневого запроса
        // именно этот обработчик отвечает что отображать,
        // когда пользователь запрашивает localhost:9889
        registerGet("/", exchange -> sendFile(exchange, makeFilePath("index.html"), ContentType.TEXT_HTML));

        // эти обрабатывают запросы с указанными расширениями
        registerFileHandler(".css", ContentType.TEXT_CSS);
        registerFileHandler(".html", ContentType.TEXT_HTML);
        registerFileHandler(".jpg", ContentType.IMAGE_JPEG);
        registerFileHandler(".png", ContentType.IMAGE_PNG);

    }

    protected final void registerGenericHandler(String method, String route, RouteHandler handler) {
        getRoutes().put(makeKey(method, route), handler);
    }

    protected final void registerGet(String route, RouteHandler handler) {
        registerGenericHandler("GET", route, handler);
    }

    protected void registerPost(String route, RouteHandler handler) {
        registerGenericHandler("POST", route, handler);
    }

    protected final void registerFileHandler(String fileExt, ContentType type) {
        registerGet(fileExt, exchange -> sendFile(exchange, makeFilePath(exchange), type));
    }

    protected final Map<String, RouteHandler> getRoutes() {
        return routes;
    }

    protected final void sendFile(HttpExchange exchange, Path pathToFile, ContentType contentType) {
        try {
            if (Files.notExists(pathToFile)) {
                respond404(exchange);
                return;
            }
            var data = Files.readAllBytes(pathToFile);
            sendByteData(exchange, ResponseCodes.OK, contentType, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path makeFilePath(HttpExchange exchange) {
        return makeFilePath(exchange.getRequestURI().getPath());
    }

    protected Path makeFilePath(String... s) {
        return Path.of(dataDir, s);
    }

    protected final void sendByteData(HttpExchange exchange, ResponseCodes responseCode,
                                      ContentType contentType, byte[] data) throws IOException {
        try (var output = exchange.getResponseBody()) {
            setContentType(exchange, contentType);
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            output.write(data);
            output.flush();
        }
    }

    private void respond404(HttpExchange exchange) {
        try {
            var data = "404 Not found".getBytes();
            sendByteData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void redirect303(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void redirect(HttpExchange exchange, String path) {
        redirect303(exchange, path);
    }

    public static String getContentType(HttpExchange exchange) {
        return exchange.getResponseHeaders()
                .getOrDefault("Content-Type", List.of("")).get(0);
    }

    protected String getBody(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        Charset utf8 = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(input, utf8);

        try (BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void handleIncomingServerRequests(HttpExchange exchange) {
        var route = getRoutes().getOrDefault(makeKey(exchange), this::respond404);
        route.handle(exchange);
    }

    public final void start() {
        server.start();
    }

    public void sendRedirect(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().set("Location", path);
            exchange.sendResponseHeaders(302, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void registerRoute(String route, RouteHandler handler) {
        getRoutes().put("GET " + route, handler);
    }
}
