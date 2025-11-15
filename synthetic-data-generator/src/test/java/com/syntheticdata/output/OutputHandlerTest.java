package com.syntheticdata.output;

import com.syntheticdata.model.Config;
import com.syntheticdata.model.KafkaConfig;
import com.syntheticdata.model.PayloadConfig;
import com.syntheticdata.publish.KafkaPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutputHandlerTest {

    @Test
    public void testStdoutHandler() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        StdoutHandler handler = new StdoutHandler();
        handler.handle("test data", new PayloadConfig());

        assertEquals("test data\n", outContent.toString());
    }

    @Test
    public void testFileHandler() throws Exception {
        PayloadConfig payloadConfig = new PayloadConfig();
        payloadConfig.setName("test");

        // Create a temporary directory for the test
        java.nio.file.Path tempDir = Files.createTempDirectory("synthetic-data-test");

        FileHandler handler = new FileHandler(tempDir.toString());
        handler.handle("test data", payloadConfig);

        assertTrue(Files.list(tempDir).count() > 0);

        // Clean up the temporary directory
        Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder()).map(java.nio.file.Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    public void testKafkaPublisher() {
        Config config = new Config();
        config.setThreadCount(1);
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.setBootstrapServers("localhost:9092");
        config.setKafka(kafkaConfig);

        KafkaProducer<String, String> mockProducer = Mockito.mock(KafkaProducer.class);
        KafkaPublisher publisher = new KafkaPublisher(config, mockProducer);

        PayloadConfig payloadConfig = new PayloadConfig();
        payloadConfig.setFile("event.transactionConcluded.json");

        publisher.handle("test data", payloadConfig);

        publisher.shutdown();

        Mockito.verify(mockProducer, Mockito.times(1)).send(Mockito.any(ProducerRecord.class));
    }
}
