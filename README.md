# user_management

user management demo

## Prerequisites

Database setup:

1- psql
2- CREATE DATABASE <db_name>;
3- create ROLE <username>;
4- ALTER USER <username> with password '<password>';
5- grant all privileges on database <db_name> to <username>;
6- ALTER ROLE "<username>" WITH LOGIN;

## Running

To start a web server for the application, run:

    lein run 

## License

Copyright © 2018 FIXME
