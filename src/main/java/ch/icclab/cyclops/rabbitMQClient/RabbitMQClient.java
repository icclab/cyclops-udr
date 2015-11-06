package ch.icclab.cyclops.rabbitMQClient; /**
 * Copyright 2014 Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @description Simple Influx-Db Client library - using maven build framework
 * @author Piyush Harsh
 * @contact: piyush.harsh@zhaw.ch
 * @date 18.08.2015
 *
 *
 */

import ch.icclab.cyclops.mcn.MCNEvent;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.influxdb.dto.BatchPoints;

import java.io.IOException;

public class RabbitMQClient implements Runnable {
    private Thread t;
    private String threadName;
    private InfluxDBClient influxDB;

    // Settings for the RabbitMQ
    private Load.RabbitMQSettings rabbitMQSettings;

    public RabbitMQClient(String name, InfluxDBClient client)
    {
        threadName = name;
        influxDB = client;
        System.out.println("Starting a new thread: " + name);

        // load rabbit MQ settings
        rabbitMQSettings = new Load().getRabbitMQSettings();
    }

    public void run()
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(rabbitMQSettings.getRabbitMQUsername());
        factory.setPassword(rabbitMQSettings.getRabbitMQPassword());
        factory.setVirtualHost(rabbitMQSettings.getRabbitMQVirtualHost());
        factory.setHost(rabbitMQSettings.getRabbitMQHost());
        factory.setPort(rabbitMQSettings.getRabbitMQPort());
        Connection connection;
        Channel channel;
        try
        {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(rabbitMQSettings.getRabbitMQQueueName(), true, false, false, null);
        }
        catch(Exception ex)
        {
            connection = null;
            channel = null;
            ex.printStackTrace();
        }
        if(channel != null)
        {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
                {
                    try
                    {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [X] Received '" + message + "'");

                        // parse object
                        Gson gson = new Gson();
                        MCNEvent data = gson.fromJson(message, MCNEvent.class);

                        // and now save data
                        BatchPoints container = influxDB.giveMeEmptyContainer();
                        container.point(data.toPoint());
                        influxDB.saveContainerToDB(container);

                    }
                    catch (Exception ex)
                    {
                        System.err.println("Caught exception in client thread: " + ex.getMessage());
                    }

                }
            };

            try
            {
                channel.basicConsume(rabbitMQSettings.getRabbitMQQueueName(), true, consumer);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        if(t == null)
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
