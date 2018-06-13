FROM devops.detainfo.com/deta/coredata-core-app-data-importor-root:v1.0.0
ADD coredata-core-app-data-importor.tar /usr/app/
ENV JAVA_OPTIONS "-Xms1024m -Xmx4096m -Dspring.profiles.active=dev -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8100,suspend=n"
ENV OVERRIDE_PROP ""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTIONS org.springframework.boot.loader.PropertiesLauncher $OVERRIDE_PROP"]