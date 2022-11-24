#!bash
shopt -s globstar dotglob nullglob

# Rebase on top of main
git checkout rewrite
git pull --rebase origin main
# Create/reset branch 'rewritten'
git checkout -B rewritten rewrite

# Switch version to 4.0.0-SNAPSHOT
echo "Switching version to 4.0.0-SNAPSHOT"
for pom in pom.xml **/pom.xml
do
  perl -i \
    -0pe 's@<version>3.20.0-SNAPSHOT</version>@<version>4.0.0-SNAPSHOT</version>@g;' \
    $pom
done
for dir in **/src/generated/resources
do
  for prop in $dir/**/*.properties
  do
    perl -i \
      -0pe 's@version=3.20.0-SNAPSHOT@version=4.0.0-SNAPSHOT@g;' \
      $prop
  done
  for json in $dir/**/*.json
  do
    perl -i \
      -0pe 's@"version": "3.20.0-SNAPSHOT"@"version": "4.0.0-SNAPSHOT"@g;' \
      $json
  done
done
git commit -a -m "Switch to 4.0.0-SNAPSHOT"

# Remove OSGi support
echo "Remove OSGi support"
for pom in pom.xml **/pom.xml
do
  perl -i \
    -0pe 's@ *<([^>]+-version-range)>[^<]+</\1> *\n@@g;' \
    -0pe 's@ *<(camel\.osgi\.[^>]+)(>[^<]+</\1>| */>) *\n@@g;' \
    -0pe 's@ *<!-- OSGi bundles properties --> *\n@@g;' \
    -0pe 's@ *<(plugin|pluginExecutionFilter)>\s*<groupId>org.apache.camel</groupId>\s*<artifactId>camel-bundle-plugin</artifactId>[\s\S]*?</\1> *\n@@g;' \
    $pom
done
perl -i -0pe 's@ *<module>init</module> *\n@@g;' pom.xml
git rm -r init
git commit -a -m "Remove OSGi support"

# Switch to javax.annotation.processing.Generated
echo "Switch to javax.annotation.processing.Generated"
for dir in **/src/{main,test,generated}
do
  for java in $dir/**/*.{java,txt,vm}
  do
    perl -i \
      -0pe 's@javax.annotation.Generated@javax.annotation.processing.Generated@g;' \
      $java
  done
done
git commit -a -m "Switch to javax.annotation.processing.Generated"

# Add support for jakarta in checkstyle
echo "Add support for jakarta in checkstyle"
for file in pom.xml etc/pom.xml buildingtools/**/camel-checkstyle.xml
do
  perl -i \
    `# Add support for jakarta package in checkstyle` \
    -0pe 's@java;javax;org.w3c;org.xml;w3c;@java;jakarta;javax;org.w3c;org.xml;w3c;@g;' \
    -0pe 's@java,javax,org.w3c,org.xml,junit@java,jakarta,javax,org.w3c,org.xml,junit@g;' \
    $file
done
git commit -a -m "Add support for jakarta in checkstyle"

# Switch javax packages to jakarta
echo "Switch javax packages to jakarta"
for dir in **/src/{main,test,generated}
do
  for java in $dir/**/*.java
  do
    perl -i \
      -0pe 's@javax.activation@jakarta.activation@g;' \
      -0pe 's@javax.annotation.security@jakarta.annotation.security@g;' \
      -0pe 's@javax.annotation.sql@jakarta.annotation.sql@g;' \
      -0pe 's@javax.annotation(?!\.processing)@jakarta.annotation@g;' \
      -0pe 's@javax.batch.api.chunk.listener@jakarta.batch.api.chunk.listener@g;' \
      -0pe 's@javax.batch.api.chunk@jakarta.batch.api.chunk@g;' \
      -0pe 's@javax.batch.api.listener@jakarta.batch.api.listener@g;' \
      -0pe 's@javax.batch.api.partition@jakarta.batch.api.partition@g;' \
      -0pe 's@javax.batch.api@jakarta.batch.api@g;' \
      -0pe 's@javax.batch.operations@jakarta.batch.operations@g;' \
      -0pe 's@javax.batch.runtime.context@jakarta.batch.runtime.context@g;' \
      -0pe 's@javax.batch.runtime@jakarta.batch.runtime@g;' \
      -0pe 's@javax.decorator@jakarta.decorator@g;' \
      -0pe 's@javax.ejb.embeddable@jakarta.ejb.embeddable@g;' \
      -0pe 's@javax.ejb.spi@jakarta.ejb.spi@g;' \
      -0pe 's@javax.ejb@jakarta.ejb@g;' \
      -0pe 's@javax.el@jakarta.el@g;' \
      -0pe 's@javax.enterprise.concurrent@jakarta.enterprise.concurrent@g;' \
      -0pe 's@javax.enterprise.context.control@jakarta.enterprise.context.control@g;' \
      -0pe 's@javax.enterprise.context.spi@jakarta.enterprise.context.spi@g;' \
      -0pe 's@javax.enterprise.context@jakarta.enterprise.context@g;' \
      -0pe 's@javax.enterprise.event@jakarta.enterprise.event@g;' \
      -0pe 's@javax.enterprise.inject.literal@jakarta.enterprise.inject.literal@g;' \
      -0pe 's@javax.enterprise.inject.se@jakarta.enterprise.inject.se@g;' \
      -0pe 's@javax.enterprise.inject.spi.configurator@jakarta.enterprise.inject.spi.configurator@g;' \
      -0pe 's@javax.enterprise.inject.spi@jakarta.enterprise.inject.spi@g;' \
      -0pe 's@javax.enterprise.inject@jakarta.enterprise.inject@g;' \
      -0pe 's@javax.enterprise.util@jakarta.enterprise.util@g;' \
      -0pe 's@javax.faces.annotation@jakarta.faces.annotation@g;' \
      -0pe 's@javax.faces.application@jakarta.faces.application@g;' \
      -0pe 's@javax.faces.bean@jakarta.faces.bean@g;' \
      -0pe 's@javax.faces.component.behavior@jakarta.faces.component.behavior@g;' \
      -0pe 's@javax.faces.component.html@jakarta.faces.component.html@g;' \
      -0pe 's@javax.faces.component.search@jakarta.faces.component.search@g;' \
      -0pe 's@javax.faces.component.visit@jakarta.faces.component.visit@g;' \
      -0pe 's@javax.faces.component@jakarta.faces.component@g;' \
      -0pe 's@javax.faces.context@jakarta.faces.context@g;' \
      -0pe 's@javax.faces.convert@jakarta.faces.convert@g;' \
      -0pe 's@javax.faces.el@jakarta.faces.el@g;' \
      -0pe 's@javax.faces.event@jakarta.faces.event@g;' \
      -0pe 's@javax.faces.flow.builder@jakarta.faces.flow.builder@g;' \
      -0pe 's@javax.faces.flow@jakarta.faces.flow@g;' \
      -0pe 's@javax.faces.lifecycle@jakarta.faces.lifecycle@g;' \
      -0pe 's@javax.faces.model@jakarta.faces.model@g;' \
      -0pe 's@javax.faces.push@jakarta.faces.push@g;' \
      -0pe 's@javax.faces.render@jakarta.faces.render@g;' \
      -0pe 's@javax.faces.validator@jakarta.faces.validator@g;' \
      -0pe 's@javax.faces.view.facelets@jakarta.faces.view.facelets@g;' \
      -0pe 's@javax.faces.view@jakarta.faces.view@g;' \
      -0pe 's@javax.faces.webapp@jakarta.faces.webapp@g;' \
      -0pe 's@javax.faces@jakarta.faces@g;' \
      -0pe 's@javax.inject@jakarta.inject@g;' \
      -0pe 's@javax.interceptor@jakarta.interceptor@g;' \
      -0pe 's@javax.jms@jakarta.jms@g;' \
      -0pe 's@javax.json.bind.adapter@jakarta.json.bind.adapter@g;' \
      -0pe 's@javax.json.bind.annotation@jakarta.json.bind.annotation@g;' \
      -0pe 's@javax.json.bind.config@jakarta.json.bind.config@g;' \
      -0pe 's@javax.json.bind.serializer@jakarta.json.bind.serializer@g;' \
      -0pe 's@javax.json.bind.spi@jakarta.json.bind.spi@g;' \
      -0pe 's@javax.json.bind@jakarta.json.bind@g;' \
      -0pe 's@javax.json.spi@jakarta.json.spi@g;' \
      -0pe 's@javax.json.stream@jakarta.json.stream@g;' \
      -0pe 's@javax.json@jakarta.json@g;' \
      -0pe 's@javax.jws.soap@jakarta.jws.soap@g;' \
      -0pe 's@javax.jws@jakarta.jws@g;' \
      -0pe 's@javax.mail.event@jakarta.mail.event@g;' \
      -0pe 's@javax.mail.internet@jakarta.mail.internet@g;' \
      -0pe 's@javax.mail.search@jakarta.mail.search@g;' \
      -0pe 's@javax.mail.util@jakarta.mail.util@g;' \
      -0pe 's@javax.mail@jakarta.mail@g;' \
      -0pe 's@javax.persistence.criteria@jakarta.persistence.criteria@g;' \
      -0pe 's@javax.persistence.metamodel@jakarta.persistence.metamodel@g;' \
      -0pe 's@javax.persistence.spi@jakarta.persistence.spi@g;' \
      -0pe 's@javax.persistence@jakarta.persistence@g;' \
      -0pe 's@javax.resource.cci@jakarta.resource.cci@g;' \
      -0pe 's@javax.resource.spi.endpoint@jakarta.resource.spi.endpoint@g;' \
      -0pe 's@javax.resource.spi.security@jakarta.resource.spi.security@g;' \
      -0pe 's@javax.resource.spi.work@jakarta.resource.spi.work@g;' \
      -0pe 's@javax.resource.spi@jakarta.resource.spi@g;' \
      -0pe 's@javax.resource@jakarta.resource@g;' \
      -0pe 's@javax.security.auth.message.callback@jakarta.security.auth.message.callback@g;' \
      -0pe 's@javax.security.auth.message.config@jakarta.security.auth.message.config@g;' \
      -0pe 's@javax.security.auth.message.module@jakarta.security.auth.message.module@g;' \
      -0pe 's@javax.security.auth.message@jakarta.security.auth.message@g;' \
      -0pe 's@javax.security.enterprise.authentication.mechanism.http@jakarta.security.enterprise.authentication.mechanism.http@g;' \
      -0pe 's@javax.security.enterprise.credential@jakarta.security.enterprise.credential@g;' \
      -0pe 's@javax.security.enterprise.identitystore@jakarta.security.enterprise.identitystore@g;' \
      -0pe 's@javax.security.enterprise@jakarta.security.enterprise@g;' \
      -0pe 's@javax.security.jacc@jakarta.security.jacc@g;' \
      -0pe 's@javax.servlet.annotation@jakarta.servlet.annotation@g;' \
      -0pe 's@javax.servlet.descriptor@jakarta.servlet.descriptor@g;' \
      -0pe 's@javax.servlet.http@jakarta.servlet.http@g;' \
      -0pe 's@javax.servlet.jsp.el@jakarta.servlet.jsp.el@g;' \
      -0pe 's@javax.servlet.jsp.jstl.core@jakarta.servlet.jsp.jstl.core@g;' \
      -0pe 's@javax.servlet.jsp.jstl.fmt@jakarta.servlet.jsp.jstl.fmt@g;' \
      -0pe 's@javax.servlet.jsp.jstl.sql@jakarta.servlet.jsp.jstl.sql@g;' \
      -0pe 's@javax.servlet.jsp.jstl.tlv@jakarta.servlet.jsp.jstl.tlv@g;' \
      -0pe 's@javax.servlet.jsp.jstl@jakarta.servlet.jsp.jstl@g;' \
      -0pe 's@javax.servlet.jsp.resources@jakarta.servlet.jsp.resources@g;' \
      -0pe 's@javax.servlet.jsp.tagext@jakarta.servlet.jsp.tagext@g;' \
      -0pe 's@javax.servlet.jsp@jakarta.servlet.jsp@g;' \
      -0pe 's@javax.servlet.resources@jakarta.servlet.resources@g;' \
      -0pe 's@javax.servlet@jakarta.servlet@g;' \
      -0pe 's@javax.transaction@jakarta.transaction@g;' \
      -0pe 's@javax.validation.bootstrap@jakarta.validation.bootstrap@g;' \
      -0pe 's@javax.validation.constraints@jakarta.validation.constraints@g;' \
      -0pe 's@javax.validation.constraintvalidation@jakarta.validation.constraintvalidation@g;' \
      -0pe 's@javax.validation.executable@jakarta.validation.executable@g;' \
      -0pe 's@javax.validation.groups@jakarta.validation.groups@g;' \
      -0pe 's@javax.validation.metadata@jakarta.validation.metadata@g;' \
      -0pe 's@javax.validation.spi@jakarta.validation.spi@g;' \
      -0pe 's@javax.validation.valueextraction@jakarta.validation.valueextraction@g;' \
      -0pe 's@javax.validation@jakarta.validation@g;' \
      -0pe 's@javax.websocket.server@jakarta.websocket.server@g;' \
      -0pe 's@javax.websocket@jakarta.websocket@g;' \
      -0pe 's@javax.ws.rs.client@jakarta.ws.rs.client@g;' \
      -0pe 's@javax.ws.rs.container@jakarta.ws.rs.container@g;' \
      -0pe 's@javax.ws.rs.core@jakarta.ws.rs.core@g;' \
      -0pe 's@javax.ws.rs.ext@jakarta.ws.rs.ext@g;' \
      -0pe 's@javax.ws.rs.sse@jakarta.ws.rs.sse@g;' \
      -0pe 's@javax.ws.rs@jakarta.ws.rs@g;' \
      -0pe 's@javax.xml.bind.annotation.adapters@jakarta.xml.bind.annotation.adapters@g;' \
      -0pe 's@javax.xml.bind.annotation@jakarta.xml.bind.annotation@g;' \
      -0pe 's@javax.xml.bind.attachment@jakarta.xml.bind.attachment@g;' \
      -0pe 's@javax.xml.bind.helpers@jakarta.xml.bind.helpers@g;' \
      -0pe 's@javax.xml.bind.util@jakarta.xml.bind.util@g;' \
      -0pe 's@javax.xml.bind@jakarta.xml.bind@g;' \
      -0pe 's@javax.xml.soap@jakarta.xml.soap@g;' \
      -0pe 's@javax.xml.ws.handler.soap@jakarta.xml.ws.handler.soap@g;' \
      -0pe 's@javax.xml.ws.handler@jakarta.xml.ws.handler@g;' \
      -0pe 's@javax.xml.ws.http@jakarta.xml.ws.http@g;' \
      -0pe 's@javax.xml.ws.soap@jakarta.xml.ws.soap@g;' \
      -0pe 's@javax.xml.ws.spi.http@jakarta.xml.ws.spi.http@g;' \
      -0pe 's@javax.xml.ws.spi@jakarta.xml.ws.spi@g;' \
      -0pe 's@javax.xml.ws.wsaddressing@jakarta.xml.ws.wsaddressing@g;' \
      -0pe 's@javax.xml.ws@jakarta.xml.ws@g;' \
      $java
  done
done
git commit -a -m "Switch javax packages to jakarta"


mvn install -DskipTests
git commit -a -m "Regen"