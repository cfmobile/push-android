package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.model.MessageReceiptData;

import java.util.List;

public interface MessageReceiptsProvider {

    List<MessageReceiptData> loadMessageReceipts();

    void saveMessageReceipts(List<MessageReceiptData> messageReceipts);
}
