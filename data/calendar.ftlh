<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Расписание на ${currentMonth}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            border: 1px solid #dee2e6;
        }
        .calendar-cell {
            min-height: 120px;
            border: 1px solid #dee2e6;
            padding: 10px;
            background-color: #fff;
            transition: background-color 0.3s;
        }
        .calendar-cell.today {
            background-color: #e7f3ff;
            position: relative;
        }
        .header-cell {
            background-color: #f8f9fa;
            font-weight: bold;
            text-align: center;
            padding: 10px;
            border: 1px solid #dee2e6;
        }
        .btn-patient { font-size: 0.8rem; padding: 0.2rem 0.5rem; }
    </style>
</head>
<body>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h1>Расписание на ${currentMonth}</h1>
        <a href="/calendar" class="btn btn-outline-primary">Текущий месяц</a>
    </div>

    <div class="calendar-grid">
        <div class="header-cell">ПН</div><div class="header-cell">ВТ</div>
        <div class="header-cell">СР</div><div class="header-cell">ЧТ</div>
        <div class="header-cell">ПТ</div><div class="header-cell">СБ</div>
        <div class="header-cell">ВС</div>

        <#list calendarDays as day>
            <div class="calendar-cell d-flex flex-column h-100 ${ (day.isToday?? && day.isToday)?string('today', '') }">
                <div class="fw-bold text-secondary">${day.dayNumber!''}</div>

                <#if day.count?? && (day.count > 0)>
                    <div class="mt-auto">
                        <a href="/patients?date=${day.date!''}" class="btn btn-primary btn-sm w-100 btn-patient">
                            ${day.count} записей
                        </a>
                    </div>
                </#if>
            </div>
        </#list>
    </div>
</div>

</body>
</html>