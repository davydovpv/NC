package ru.ncapital.gateways.moexfast.connection.messageprocessors.sequencevalidators;

import ru.ncapital.gateways.moexfast.messagehandlers.MessageHandlerType;

/**
 * Created by egore on 2/3/16.
 */
public class MessageSequenceValidatorForOrderBook<T> extends MessageSequenceValidator<T> {
    public MessageSequenceValidatorForOrderBook() {
        super(MessageHandlerType.ORDER_BOOK);
    }
}
