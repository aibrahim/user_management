FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/user_management.jar /user_management/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/user_management/app.jar"]
