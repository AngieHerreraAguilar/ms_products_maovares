package com.maovares.ms_products.product.infraestructure.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class QueueService {

    private QueueClient queueClient;

    public QueueService(@Value("${AZURE_STORAGE_CONNECTION_STRING:}") String connectionString) {
        if (connectionString != null && !connectionString.isEmpty()) {
            this.queueClient = new QueueClientBuilder()
                    .connectionString(connectionString)
                    .queueName("ordersqueue")
                    .buildClient();
        }
    }

    public void sendMessage(String message) {
        if (queueClient == null) {
            return;
        }
        String encoded = Base64.getEncoder().encodeToString(message.getBytes());
        queueClient.sendMessage(encoded);
    }
}