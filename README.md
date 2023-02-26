# Дневник студента<br>
### Описание возможностей, которые будут в приложении:<br>
- Наличие групп с общим списком задач и чатом.

- Возможность создавать личные папки, а потом при желании переводить их в группы.

- Настройка прав доступа в группе.

- Можно создать запись* в своей папке и при желании поделиться ею со всей группой.

- Возможность закрывать задачи нажатием на галочку (если она там есть).

- (Уникальная, настраиваемая) Пользователь в группе при изменении состояния задачи не изменяет ее статус у других участников. <br> 
Таким образом не придется создавать по подзадаче для каждого участника группы. <br>
Пример: Студент выполнил домашнее задание, нажал галочку напротив него, оно у него исчезло, а у остальных, у кого это задание осталось, запись не исчезает.

- Возможность выставлять разные уведомления как для общей задачи, так и личной.

- Возможность создать повторяющиеся задачи.

- Наличие сортировки по категориям, тегам.

- Автономность и сохранение полученных данных.

- Данные о записях группы обновляются при изменении/добавлении записи

- (В будущем) Добавление вложений или комментарий к записи.

- (В будущем) Если пользователь не имеет права создавать задачу, то у него может быть возможность предложить её на усмотрение.


**Запись** - заметка или задача

### Стек технологий:
- Платформа: Android версии от 4.0 до последней
- Язык программирования: Kotlin
- База данных: Firebase Firestore (NoSQL)
- Бэкенд: Firebase

### Текущее состояние:
- Параллельно разрабатывается логика по взаимодействию с БД и список задач
- Создано 16 функций по взаимодействию с БД (Ветка master)
- Имеется демо-вариант списка задач (ветка UI)
- Дизайн приложения по ссылке https://www.figma.com/file/97eP7T0TROGFlC2U5qWu2C/Untitled?node-id=0%3A1&t=G1FQPxbKZ2OhmM8l-1
