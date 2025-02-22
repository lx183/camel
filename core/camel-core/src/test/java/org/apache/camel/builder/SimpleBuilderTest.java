/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.TestSupport;
import org.apache.camel.TypeConversionException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleBuilderTest extends TestSupport {

    protected Exchange exchange = new DefaultExchange(new DefaultCamelContext());

    @Test
    public void testPredicate() throws Exception {
        exchange.getIn().setBody("foo");

        assertTrue(SimpleBuilder.simple("${body} == 'foo'").matches(exchange));
        assertFalse(SimpleBuilder.simple("${body} == 'bar'").matches(exchange));
    }

    @Test
    public void testExpression() throws Exception {
        exchange.getIn().setBody("foo");

        assertEquals("foo", SimpleBuilder.simple("${body}").evaluate(exchange, String.class));
        assertNull(SimpleBuilder.simple("${header.cheese}").evaluate(exchange, String.class));
    }

    @Test
    public void testFormatExpression() throws Exception {
        exchange.getIn().setHeader("head", "foo");

        assertEquals("foo", SimpleBuilder.simpleF("${header.%s}", "head").evaluate(exchange, String.class));
        assertNull(SimpleBuilder.simple("${header.cheese}").evaluate(exchange, String.class));
    }

    @Test
    public void testFormatExpressionWithResultType() throws Exception {
        exchange.getIn().setHeader("head", "200");

        assertEquals(200, SimpleBuilder.simpleF("${header.%s}", Integer.class, "head").evaluate(exchange, Object.class));
    }

    @Test
    public void testResultType() throws Exception {
        exchange.getIn().setBody("foo");
        exchange.getIn().setHeader("cool", true);

        assertEquals("foo", SimpleBuilder.simple("${body}", String.class).evaluate(exchange, Object.class));
        try {
            // error during conversion
            SimpleBuilder.simple("${body}", int.class).evaluate(exchange, Object.class);
            fail("Should have thrown exception");
        } catch (TypeConversionException e) {
            assertIsInstanceOf(NumberFormatException.class, e.getCause());
        }

        assertEquals(true, SimpleBuilder.simple("${header.cool}", boolean.class).evaluate(exchange, Object.class));
        assertEquals("true", SimpleBuilder.simple("${header.cool}", String.class).evaluate(exchange, Object.class));
        // not possible
        assertNull(SimpleBuilder.simple("${header.cool}", int.class).evaluate(exchange, Object.class));

        assertEquals(true, SimpleBuilder.simple("${header.cool}").resultType(Boolean.class).evaluate(exchange, Object.class));
        assertEquals("true", SimpleBuilder.simple("${header.cool}").resultType(String.class).evaluate(exchange, Object.class));
        // not possible
        assertNull(SimpleBuilder.simple("${header.cool}").resultType(int.class).evaluate(exchange, Object.class));

        // should be convertable to integers
        assertEquals(11, SimpleBuilder.simple("11", int.class).evaluate(exchange, Object.class));
    }

    @Test
    public void testRegexAllWithPlaceHolders() {
        exchange.getIn().setHeader("activateUrl", "http://some/rest/api/(id)/activate");
        assertEquals("http://some/rest/api/12/activate",
                SimpleBuilder.simple("${header.activateUrl.replaceAll(\"\\(id\\)\",\"12\")}").evaluate(exchange, String.class));

        // passes when contains { only
        exchange.getIn().setHeader("activateUrl", "http://some/rest/api/{id/activate");
        assertEquals("http://some/rest/api/12/activate",
                SimpleBuilder.simple("${header.activateUrl.replaceAll(\"\\{id\",\"12\")}").evaluate(exchange, String.class));

        String replaced = "http://some/rest/api/{id}/activate".replaceAll("\\{id\\}", "12");
        assertEquals("http://some/rest/api/12/activate", replaced);

        // passes when contains { }
        exchange.getIn().setHeader("activateUrl", "http://some/rest/api/{id}/activate");
        assertEquals("http://some/rest/api/12/activate",
                SimpleBuilder.simple("${header.activateUrl.replaceAll(\"\\{id\\}\",\"12\")}").evaluate(exchange, String.class));

        // passes when contains { } and another ${body} function
        exchange.getIn().setBody("12");
        assertEquals("http://some/rest/api/12/activate", SimpleBuilder
                .simple("${header.activateUrl.replaceAll(\"\\{id\\}\",\"${body}\")}").evaluate(exchange, String.class));

        // passes when } is escaped with \}
        assertEquals("http://some/rest/api/{}/activate", SimpleBuilder
                .simple("${header.activateUrl.replaceAll(\"\\{id\\}\",\"{\\}\")}").evaluate(exchange, String.class));
    }

    @Test
    public void testPropertyPlaceholder() throws Exception {
        exchange.getIn().setBody("Hello");

        Properties prop = new Properties();
        prop.put("foo", "bar");
        PropertiesComponent pc = exchange.getContext().getPropertiesComponent();
        pc.setOverrideProperties(prop);

        assertEquals("bar", SimpleBuilder.simple("{{foo}}").evaluate(exchange, String.class));
        assertEquals("bar", SimpleBuilder.simple("${properties:foo}").evaluate(exchange, String.class));

        try {
            SimpleBuilder.simple("{{bar}}").evaluate(exchange, String.class);
            fail("Should fail");
        } catch (Exception e) {
            // expected
        }
        try {
            SimpleBuilder.simple("${properties:bar}").evaluate(exchange, String.class);
            fail("Should fail");
        } catch (Exception e) {
            // expected
        }
    }

}
