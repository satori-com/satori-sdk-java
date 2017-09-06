package com.satori.rtm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.satori.rtm.model.Pdu;
import com.satori.rtm.model.ReadReply;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class KVStorageTest extends AbstractRealTest {
  @Test
  public void readAfterPublish() throws ExecutionException, InterruptedException {
    RtmClient client = clientBuilder().build();
    client.start();

    awaitFuture(client.publish(channel, "message", Ack.YES));
    Pdu<ReadReply> pdu = awaitFuture(client.read(channel));
    assertThat(pdu.getBody().getMessageAsType(String.class), equalTo("message"));
    client.stop();
  }

  @Test
  public void doubleWriteAndOneRead() throws ExecutionException {
    RtmClient client = clientBuilder().build();
    client.start();
    awaitFuture(client.write(channel, "value1", Ack.YES));
    awaitFuture(client.write(channel, "value2", Ack.YES));
    Pdu<ReadReply> pdu = awaitFuture(client.read(channel));
    assertThat(pdu.getBody().getMessageAsType(String.class), equalTo("value2"));
    client.stop();
  }

  @Test
  public void writeDeleteRead() throws ExecutionException {
    RtmClient client = clientBuilder().build();
    client.start();

    awaitFuture(client.read(channel));
    awaitFuture(client.write(channel, "value", Ack.YES));
    awaitFuture(client.write(channel, "value2", Ack.YES));

    Pdu<ReadReply> pdu = awaitFuture(client.read(channel));
    assertThat(pdu.getBody().getMessageAsType(String.class), equalTo("value2"));

    awaitFuture(client.delete(channel, Ack.YES));
    pdu = awaitFuture(client.read(channel));
    assertThat(pdu.getBody().getMessageAsType(String.class), is(nullValue()));

    client.stop();
  }
}
