<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Добавление пациента</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card p-4 shadow">
        <h3>Добавить пациента на ${date}</h3>
        <form action="/add-patient" method="POST">
            <input type="hidden" name="date" value="${date}">

            <div class="row">
                <div class="col-md-4 mb-3">
                    <label>Фамилия:</label>
                    <input type="text" name="lastName" class="form-control" value="${generate.lastName}" required>
                </div>
                <div class="col-md-4 mb-3">
                    <label>Имя:</label>
                    <input type="text" name="firstName" class="form-control" value="${generate.name}" required>
                </div>
                <div class="col-md-4 mb-3">
                    <label>Отчество:</label>
                    <input type="text" name="patronymic" class="form-control" value="${generate.patronymic}" required>
                </div>
            </div>

            <div class="mb-3">
                <label>Время:</label>
                <input type="time" name="time" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>Дата рождения:</label>
                <input type="date" name="birthDate" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>Тип пациента:</label>
                <select name="type" class="form-select">
                    <option value="Первичный">Первичный</option>
                    <option value="Вторичный">Вторичный</option>
                </select>
            </div>

            <div class="mb-3">
                <label>Анамнез:</label>
                <input type="text" name="symptoms" class="form-control" value="${generate.anamnez}">
            </div>

            <button type="submit" class="btn btn-primary w-100">Сохранить запись</button>
            <a href="/patients?date=${date}" class="btn btn-link w-100 mt-2">Отмена</a>
        </form>
    </div>
</div>
</body>
</html>