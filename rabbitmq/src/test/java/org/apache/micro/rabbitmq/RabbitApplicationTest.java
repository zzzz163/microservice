package org.apache.micro.rabbitmq;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = { RabbitmqApplication.class })
public class RabbitApplicationTest {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper objectMapper ;
	
	@Autowired
	private CachingConnectionFactory connectionFactory ;

	private String routeKey = "foo";

	private Exchange exchange;

	private AtomicInteger cack = new AtomicInteger(); 
	
	private AtomicInteger cnack = new AtomicInteger(); 
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSend() {
		
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
			
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				if(ack){
					cack.incrementAndGet() ;
				}else{
					cnack.incrementAndGet() ;
					System.out.println(cause);
				}
				
			}
		});
		
//		rabbitTemplate.setReturnCallback(new ReturnCallback() {
//			
//			@Override
//			public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//				count.incrementAndGet() ;
//				
//			}
//		});
		
//		rabbitTemplate.setReturnCallback(new );
		TestDomain domain = new TestDomain() ;
		domain.setIdno("350525198611014972");
		domain.setName("黄跃文");
		domain.setAddress("福建省泉州市永春县石鼓镇半岭村3组810号");
		domain.setBirth(new Date());
		try {
//			String message = objectMapper.writeValueAsString(domain) ;
			long start = System.currentTimeMillis() ;
//			int pool = 1 ;
//			ExecutorService cservice = Executors.newFixedThreadPool(pool) ;
//			for(int i = 0 ;i < pool ; i++){
//				cservice.execute(new P(message));
//			}
//			cservice.shutdown();
//			while(!cservice.isTerminated()){
//			}
			
			for(int i =0 ;i< 1 ;i++){
//				Object result = rabbitTemplate.convertSendAndReceive(routeKey, message);
//				System.out.println(result);
				domain.setIdno(domain.getIdno()+i);
				rabbitTemplate.convertAndSend("amq.direct","hello", objectMapper.writeValueAsString(domain) );
			}
			
			
			System.out.println("ack and nack "+cack.get()+" "+cnack.get());
			System.out.println("publisher cost:"+(System.currentTimeMillis()-start));
			
			Thread.sleep(6000l);
			
			System.out.println("ack and nack "+cack.get()+" "+cnack.get());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class P implements Runnable{
		
		private String message ;
		
		private P(String message) {
			super();
			this.message = message;
		}


		@Override
		public void run() {
			for(int i =0 ;i< 10000 ;i++){
//				Object result = rabbitTemplate.convertSendAndReceive(routeKey, message);
//				System.out.println(result);
				rabbitTemplate.convertAndSend(routeKey, message);
//				rabbitTemplate.
			}
			
		}
		
	}

	@Test
	public void testReceive() {
//		Message message = rabbitTemplate.receive("hello") ;
//		System.out.println(new String(message.getBody()));
//		rabbitTemplate.setChannelTransacted(true);
//		Channel channel = rabbitTemplate.getConnectionFactory().createConnection().createChannel(false) ;
//		channel.
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {

			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				System.out.println("afasdfasdf "+ack+" "+cause);

			}
		});
		boolean receive = rabbitTemplate.receiveAndReply("hello",new ReceiveAndReplyCallback<String, Void>() {

			@Override
			public Void handle(String arg0) {
				System.out.println(arg0);

				return null;
			}
		});
	}
	
	@Test
	public void testAsynReceive(){
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory) ;
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		container.setConcurrentConsumers(1);
		container.setQueueNames("hello");
		container.setMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(Message arg0) {
				String message = new String(arg0.getBody(),Charset.forName("utf-8")) ;
				System.out.println(message);
			}
		});
		
		
		try {
			Thread.currentThread().sleep(3000l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
