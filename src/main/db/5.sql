select users.name, count(*)
from users join messages on users.id = user_id
group by users.name
having count(*) > 3