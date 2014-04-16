package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.List;

public class CleanupEventsJob extends BaseJob {

    public CleanupEventsJob() {
        super();
    }

    @Override
    public void run(JobParams jobParams) {
        cleanupDatabase(jobParams);
        enableAlarmIfRequired(jobParams);
        jobParams.listener.onJobComplete(JobResultListener.RESULT_SUCCESS);
    }

    private void cleanupDatabase(JobParams jobParams) {
        int numberOfFixedEvents = 0;
        numberOfFixedEvents += fixEventsWithStatus(BaseEvent.Status.POSTING, jobParams);
        numberOfFixedEvents += deleteEventsWithStatus(BaseEvent.Status.POSTED, jobParams);
        if (numberOfFixedEvents <= 0) {
            PushLibLogger.fd("CleanupEventsJob: no events in the database that need to be cleaned.", numberOfFixedEvents);
        }
    }

    // TODO - generalize to all event types
    private int fixEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, status);
        if (uris.size() > 0) {
            for (final Uri uri : uris) {
                jobParams.eventsStorage.setEventStatus(uri, BaseEvent.Status.NOT_POSTED);
            }
            PushLibLogger.fd("CleanupEventsJob: set %d '%s' events to status '%s'", uris.size(), BaseEvent.statusString(status), BaseEvent.statusString(BaseEvent.Status.NOT_POSTED));
        }
        return uris.size();
    }

    // TODO - generalize to all event types
    private int deleteEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, status);
        jobParams.eventsStorage.deleteEvents(uris, EventsStorage.EventType.MESSAGE_RECEIPT);
        if (uris.size() > 0) {
            PushLibLogger.fd("CleanupEventsJob: deleted %d events with status '%s'", uris.size(), BaseEvent.statusString(status));
        }
        return uris.size();
    }

    // TODO - generalize to all event types
    private void enableAlarmIfRequired(JobParams jobParams) {
        int numberOfPendingMessageReceipts = 0;
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.NOT_POSTED).size();
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.POSTING_ERROR).size();
        if (numberOfPendingMessageReceipts > 0) {
            PushLibLogger.fd("CleanupEventsJob: There are %d events(s) queued for sending. Enabling alarm.", numberOfPendingMessageReceipts);
            jobParams.alarmProvider.enableAlarmIfDisabled();
        } else {
            PushLibLogger.d("CleanupEventsJob: There are no events queued for sending. Disabling alarm.");
            jobParams.alarmProvider.disableAlarm();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof CleanupEventsJob)) {
            return false;
        }
        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<CleanupEventsJob> CREATOR = new Parcelable.Creator<CleanupEventsJob>() {

        public CleanupEventsJob createFromParcel(Parcel in) {
            return new CleanupEventsJob(in);
        }

        public CleanupEventsJob[] newArray(int size) {
            return new CleanupEventsJob[size];
        }
    };

    private CleanupEventsJob(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
