select messages.id, text, users.name, messages.date
from messages join users on user_id = users.id
where users.name = 'Jan' and date(messages.date) = '2015-05-02 *'