package ru.ncapital.gateways.moexfast.connection.messageprocessors;

/**
 * Created by egore on 5/24/16.
 */
import ru.ncapital.gateways.moexfast.connection.messageprocessors.sequencevalidators.IMessageSequenceValidator;

public interface ISnapshotProcessor extends IProcessor {
    void reset();

    IMessageSequenceValidator getSequenceValidator();
}
