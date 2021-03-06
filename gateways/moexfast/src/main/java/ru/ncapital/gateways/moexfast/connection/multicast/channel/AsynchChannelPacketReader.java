package ru.ncapital.gateways.moexfast.connection.multicast.channel;

import ru.ncapital.gateways.moexfast.connection.ConnectionId;
import ru.ncapital.gateways.moexfast.connection.multicast.IMulticastEventListener;

import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by egore on 12/9/15.
 */
public class AsynchChannelPacketReader extends AChannelPacketReader {

    private BlockingQueue<ChannelPacket> packetQueue;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ConnectionId connectionId;

    public AsynchChannelPacketReader(IMulticastEventListener eventListener, DatagramChannel channel, BlockingQueue queue, ConnectionId connectionId) {
        super(eventListener, channel);

        this.packetQueue = queue;
        this.connectionId = connectionId;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        running = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName(connectionId.toString() + "-asynch-reader");
                while (running) {
                    try {
                        ChannelPacket channelPacket = receivePacketFromChannel();
                        if (channelPacket != null)
                            packetQueue.offer(channelPacket);

                    } catch (Exception e) {
                        eventReceiver.onException(e);
                        running = false;
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        int count = 50;
        running = false;
        executor.shutdown();
        try {
            while (!executor.isTerminated() && --count >= 0) {
                Thread.sleep(100);
            }

            if (!executor.isTerminated()) {
                executor.shutdownNow();

                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            eventReceiver.onException(e);
        }
    }

    @Override
    public ChannelPacket nextPacket() {
        if (running) {
            try {
                return packetQueue.take();
            } catch (InterruptedException e) {
                eventReceiver.onException(e);
            }
        }
        return null;
    }
}
