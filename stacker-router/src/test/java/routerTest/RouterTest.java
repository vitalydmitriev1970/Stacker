package routerTest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stacker.common.dto.Command;
import stacker.common.ICallback;
import stacker.router.Router;
import stacker.router.SessionStack;

import static org.junit.Assert.assertEquals;


public class RouterTest {
    private static Logger log = LoggerFactory.getLogger(RouterTest.class);

    @Test
    public void Test() {
        MockTransport transport = new MockTransport();
        MockSessionStorage sessionStorage = new MockSessionStorage();

        Router router = new Router(transport, sessionStorage);

        router.addFlow("main", "http://main.flow");
        router.setMainFlow("main");
        router.addFlow("second", "http://main.flow");
        router.setFlowMapping("main", "main", "second");

        ///////////////////////////////////////
        String sid = "111";

        Command respCommand = new Command();
        respCommand.setType(Command.Type.QUESTION);
        respCommand.setContentBody("Response body".getBytes());
        respCommand.setState("entry");
        respCommand.setFlow("main");
        respCommand.setFlowData("session Data".getBytes());

        transport.setRespCommand(respCommand);


        router.handleRequest(sid, "Hello world".getBytes(), new Router.IRouterCallback() {
            @Override
            public void success(String sid, byte[] body) {
                assertEquals("Response body", new String(body));
                Command lastRequest = transport.lastRequest;
                assertEquals("Hello world", new String(lastRequest.getContentBody()));
                assertEquals(Command.Type.OPEN, lastRequest.getType());
                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessiondata = sessionStackEntries.peek().getFlowData();
                        assertEquals("session Data", new String(sessiondata));
                    }

                    @Override
                    public void reject(Exception error) {

                    }
                });
            }

            @Override
            public void reject(Exception exception) {

            }
        });

        respCommand.setType(Command.Type.OPEN);

        router.handleRequest(sid, "Hello again".getBytes(), new Router.IRouterCallback() {

            @Override
            public void success(String sid, byte[] body) {
                Command lastRequest = transport.lastRequest;
                assertEquals("Hello again", new String(lastRequest.getContentBody()));
                assertEquals("ANSWER", lastRequest.getType().toString());
                assertEquals("session Data", new String(lastRequest.getFlowData()));
                sessionStorage.find(sid, new ICallback<SessionStack>() {

                    @Override
                    public void success(SessionStack sessionStackEntries) {
                        assertEquals(1, sessionStackEntries.size());
                        byte[] sessiondata = sessionStackEntries.peek().getFlowData();
                        assertEquals("session Data", new String(sessiondata));
                    }

                    @Override
                    public void reject(Exception error) {

                    }
                });
            }

            @Override
            public void reject(Exception exception) {

            }
        });
    }
}
