package test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.grizzly.GrizzlyNetwork;
import se.sics.kompics.network.grizzly.GrizzlyNetworkInit;

public class TestClient extends ComponentDefinition {

	public static void main(String[] args) {
		Kompics.createAndStart(TestClient.class);
	}

	Address c = new Address(InetAddress.getLocalHost(), 3333, 0);
	Address s = new Address(InetAddress.getLocalHost(), 2222, 0);
	Address mcast;

	Component netty;

	public TestClient() throws UnknownHostException {
		netty = create(GrizzlyNetwork.class);
		subscribe(start, control);
		subscribe(h, netty.provided(Network.class));
		trigger(new GrizzlyNetworkInit(c), netty.control());
	}

	Handler<Start> start = new Handler<Start>() {
		public void handle(Start event) {

			String message = "Hello!";

			TestMessage tm = new TestMessage(c, s, message.getBytes());

			trigger(tm, netty.provided(Network.class));

			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos;
				oos = new ObjectOutputStream(baos);
				oos.writeObject(tm);
				oos.flush();
				oos.close();

				byte[] buf = baos.toByteArray();

				System.err.println("Sent " + tm + " in " + buf.length
						+ " bytes [" + new String(buf) + "]");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	Handler<TestMessage> h = new Handler<TestMessage>() {
		public void handle(TestMessage event) {
			System.err.println("Received " + event.getPayload().length
					+ " bytes " + new String(event.getPayload()));

//			trigger(new TestMessage(event.getDestination(), event.getSource(),
//					"Hello".getBytes()), netty.provided(Network.class));
		}
	};

}
