<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Пациенты на ${date}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Пациенты на ${date}</h1>
        <div>
            <a href="/add-patient-page?date=${date}" class="btn btn-success">+ Добавить пациента</a>
            <a href="/calendar" class="btn btn-secondary">Назад к календарю</a>
        </div>
    </div>


    <#if query??>
        <#if query?contains("error=interval")>
            <div class="alert alert-danger mb-4">Ошибка: Прием пациента длится до 20 минут, выберите свободное время</div>
        <#elseif query?contains("error=past")>
            <div class="alert alert-danger mb-4">Ошибка: Вы выбрали прошедшее время.</div>
        <#elseif query?contains("error=date")>
            <div class="alert alert-danger mb-4">Ошибка: Некорректная дата рождения.</div>
        <#elseif query?contains("error=working_hours")>
            <div class="alert alert-danger mb-4">Ошибка: Запись доступна только с 09:00 до 18:00.</div>
        </#if>
    </#if>

    <#if patients?has_content>
        <table class="table table-hover table-bordered">
            <thead class="table-light">
                <tr><th>Время</th><th>ФИО</th><th>Дата рождения</th><th>Тип</th><th>Анамнез</th><th>Действие</th></tr>
            </thead>
            <tbody>
                <#list patients as patient>
                    <tr>
                        <td class="align-middle">${patient.time}</td>
                        <td class="align-middle fw-bold">${patient.fullName}</td>
                        <td class="align-middle">${patient.birthDate}</td>
                        <td class="align-middle">
                            <span class="badge ${(patient.type == 'Первичный')?string('bg-success', 'bg-warning text-dark')}">${patient.type}</span>
                        </td>
                        <td class="align-middle">${patient.symptoms}</td>
                        <td class="align-middle text-center">
                            <form action="/delete-patient" method="POST">
                                <input type="hidden" name="fullName" value="${patient.fullName}">
                                <input type="hidden" name="date" value="${date}">
                                <button type="submit" class="btn btn-danger btn-sm">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    <#else>
        <div class="alert alert-info">На этот день записей нет.</div>
    </#if>
</div>
</body>
</html>