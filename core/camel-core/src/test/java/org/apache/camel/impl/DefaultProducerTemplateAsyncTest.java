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
package org.apache.camel.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.SynchronizationAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultProducerTemplateAsyncTest extends ContextTestSupport {

    @Test
    public void testRequestAsync() throws Exception {
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("Hello");

        long start = System.currentTimeMillis();
        Future<Exchange> future = template.asyncSend("direct:start", exchange);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        Exchange result = future.get();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result.getIn().getBody());
        assertTrue(delta > 50, "Should take longer than: " + delta);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSendAsyncProcessor() throws Exception {
        Future<Exchange> future = template.asyncSend("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Hello");
            }
        });
        long start = System.currentTimeMillis();

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        Exchange result = future.get();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result.getIn().getBody());
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBody() throws Exception {
        long start = System.currentTimeMillis();
        Future<Object> future = template.asyncRequestBody("direct:start", "Hello");

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // we can use extract body to convert to expect body type
        String result = template.extractFutureBody(future, String.class);

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyType() throws Exception {
        long start = System.currentTimeMillis();
        Future<String> future = template.asyncRequestBody("direct:start", "Hello", String.class);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // or we can use parameter type in the requestBody method so the future
        // handle know its type
        String result = future.get();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyAndHeader() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("foo", 123);

        long start = System.currentTimeMillis();
        Future<Object> future = template.asyncRequestBodyAndHeader("direct:start", "Hello", "foo", 123);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // we can use extract body to convert to expect body type
        String result = template.extractFutureBody(future, String.class);

        assertMockEndpointsSatisfied();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyAndHeaderType() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("foo", 123);

        long start = System.currentTimeMillis();
        Future<String> future = template.asyncRequestBodyAndHeader("direct:start", "Hello", "foo", 123, String.class);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // or we can use parameter type in the requestBody method so the future
        // handle know its type
        String result = future.get();

        assertMockEndpointsSatisfied();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyAndHeaders() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("foo", 123);
        mock.expectedHeaderReceived("bar", "cheese");

        Map<String, Object> headers = new HashMap<>();
        headers.put("foo", 123);
        headers.put("bar", "cheese");
        long start = System.currentTimeMillis();
        Future<Object> future = template.asyncRequestBodyAndHeaders("direct:start", "Hello", headers);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // we can use extract body to convert to expect body type
        String result = template.extractFutureBody(future, String.class);

        assertMockEndpointsSatisfied();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyAndHeadersType() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("foo", 123);
        mock.expectedHeaderReceived("bar", "cheese");

        Map<String, Object> headers = new HashMap<>();
        headers.put("foo", 123);
        headers.put("bar", "cheese");
        long start = System.currentTimeMillis();
        Future<String> future = template.asyncRequestBodyAndHeaders("direct:start", "Hello", headers, String.class);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        // or we can use parameter type in the requestBody method so the future
        // handle know its type
        String result = future.get();

        assertMockEndpointsSatisfied();

        long delta = System.currentTimeMillis() - start;
        assertEquals("Hello World", result);
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncErrorWhenProcessing() throws Exception {
        Exchange exchange = new DefaultExchange(context);
        exchange.getIn().setBody("Hello");

        long start = System.currentTimeMillis();
        Future<Object> future = template.asyncRequestBody("direct:error", exchange);

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        try {
            template.extractFutureBody(future, Exchange.class);
            fail("Should have thrown exception");
        } catch (RuntimeCamelException e) {
            assertEquals("Damn forced by unit test", e.getCause().getMessage());
        }

        long delta = System.currentTimeMillis() - start;
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testRequestAsyncBodyErrorWhenProcessing() throws Exception {
        long start = System.currentTimeMillis();
        Future<Object> future = template.asyncRequestBody("direct:error", "Hello");

        // you can do other stuff
        String echo = template.requestBody("direct:echo", "Hi", String.class);
        assertEquals("HiHi", echo);

        try {
            template.extractFutureBody(future, String.class);
            fail("Should have thrown exception");
        } catch (RuntimeCamelException e) {
            assertEquals("Damn forced by unit test", e.getCause().getMessage());
        }

        long delta = System.currentTimeMillis() - start;
        assertTrue(delta > 50, "Should take longer than: " + delta);
    }

    @Test
    public void testAsyncCallbackExchangeInOnly() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        final CountDownLatch latch = new CountDownLatch(1);

        Exchange exchange = context.getEndpoint("direct:start").createExchange();
        exchange.getIn().setBody("Hello");

        template.asyncCallback("direct:start", exchange, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("Hello World", exchange.getIn().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertMockEndpointsSatisfied();
        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackExchangeInOut() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        Exchange exchange = context.getEndpoint("direct:start").createExchange();
        exchange.getIn().setBody("Hello");
        exchange.setPattern(ExchangePattern.InOut);

        template.asyncCallback("direct:echo", exchange, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("HelloHello", exchange.getMessage().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackExchangeInOnlyGetResult() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        Exchange exchange = context.getEndpoint("direct:start").createExchange();
        exchange.getIn().setBody("Hello");

        Future<Exchange> future = template.asyncCallback("direct:start", exchange, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("Hello World", exchange.getIn().getBody());
                order.addAndGet(2);
            }
        });

        order.addAndGet(1);
        Exchange reply = future.get(10, TimeUnit.SECONDS);
        order.addAndGet(4);

        assertMockEndpointsSatisfied();
        assertEquals(7, order.get());
        assertNotNull(reply);
    }

    @Test
    public void testAsyncCallbackExchangeInOutGetResult() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        Exchange exchange = context.getEndpoint("direct:start").createExchange();
        exchange.getIn().setBody("Hello");
        exchange.setPattern(ExchangePattern.InOut);

        Future<Exchange> future = template.asyncCallback("direct:echo", exchange, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("HelloHello", exchange.getMessage().getBody());
                order.addAndGet(2);
            }
        });

        order.addAndGet(1);
        Exchange reply = future.get(10, TimeUnit.SECONDS);
        order.addAndGet(4);

        assertEquals(7, order.get());
        assertNotNull(reply);
        assertEquals("HelloHello", reply.getMessage().getBody());
    }

    @Test
    public void testAsyncCallbackBodyInOnly() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        final CountDownLatch latch = new CountDownLatch(1);

        template.asyncCallbackSendBody("direct:start", "Hello", new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("Hello World", exchange.getIn().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertMockEndpointsSatisfied();
        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackBodyInOut() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        template.asyncCallbackRequestBody("direct:echo", "Hello", new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("HelloHello", exchange.getMessage().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackBodyInOnlyGetResult() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        Future<Object> future = template.asyncCallbackSendBody("direct:start", "Hello", new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("Hello World", exchange.getIn().getBody());
                order.addAndGet(2);
            }
        });

        order.addAndGet(1);
        Object reply = future.get(10, TimeUnit.SECONDS);
        order.addAndGet(4);

        assertMockEndpointsSatisfied();
        assertEquals(7, order.get());
        // no reply when in only
        assertNull(reply);
    }

    @Test
    public void testAsyncCallbackBodyInOutGetResult() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        Future<Object> future = template.asyncCallbackRequestBody("direct:echo", "Hello", new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("HelloHello", exchange.getMessage().getBody());
                order.addAndGet(2);
            }
        });

        order.addAndGet(1);
        Object reply = future.get(10, TimeUnit.SECONDS);
        order.addAndGet(4);

        assertEquals(7, order.get());
        assertEquals("HelloHello", reply);
    }

    @Test
    public void testAsyncCallbackInOnlyProcessor() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        final CountDownLatch latch = new CountDownLatch(1);

        template.asyncCallback("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Hello");
            }
        }, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("Hello World", exchange.getIn().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertMockEndpointsSatisfied();
        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackInOutProcessor() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        template.asyncCallback("direct:echo", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Hello");
                exchange.setPattern(ExchangePattern.InOut);
            }
        }, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("HelloHello", exchange.getMessage().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackThreadsInOutProcessor() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        template.asyncCallback("direct:threads", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Bye");
                exchange.setPattern(ExchangePattern.InOut);
            }
        }, new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                assertEquals("ByeBye", exchange.getMessage().getBody());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackExchangeInOnlyWithFailure() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        Exchange exchange = context.getEndpoint("direct:error").createExchange();
        exchange.getIn().setBody("Hello");

        template.asyncCallback("direct:error", exchange, new SynchronizationAdapter() {
            @Override
            public void onFailure(Exchange exchange) {
                assertEquals("Damn forced by unit test", exchange.getException().getMessage());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Test
    public void testAsyncCallbackExchangeInOutWithFailure() throws Exception {
        final AtomicInteger order = new AtomicInteger();

        final CountDownLatch latch = new CountDownLatch(1);

        Exchange exchange = context.getEndpoint("direct:error").createExchange();
        exchange.getIn().setBody("Hello");
        exchange.setPattern(ExchangePattern.InOut);

        template.asyncCallback("direct:error", exchange, new SynchronizationAdapter() {
            @Override
            public void onFailure(Exchange exchange) {
                assertEquals("Damn forced by unit test", exchange.getException().getMessage());
                order.addAndGet(2);
                latch.countDown();
            }
        });

        order.addAndGet(1);
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        order.addAndGet(4);

        assertEquals(7, order.get());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").delay(200).asyncDelayed().transform(body().append(" World")).to("mock:result");

                from("direct:error").delay(200).asyncDelayed().process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        throw new IllegalArgumentException("Damn forced by unit test");
                    }
                });

                from("direct:echo").transform(body().append(body()));

                from("direct:threads").threads(5).transform(body().append(body()));
            }
        };
    }

}
