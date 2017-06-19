# Functional DB access with Doobie

This repo contains a small Scala project to do a workshop in the
Scala BCN meetup around some of the basic capabilities of
[doobie](https://github.com/tpolecat/doobie).

In order to run it you'll need a postgres db locally. The easiest
way to have it running is probably using docker:

`$ docker run -d --name doobie-doo -p 5432:5432 postgres`

This will start a database server listening to port `5432`
with password `mysecretpassword`. You can change it by setting
the `POSTGRES_PASSWORD` environment variable. Check
[https://hub.docker.com/_/postgres/](https://hub.docker.com/_/postgres/)
for more options.
 