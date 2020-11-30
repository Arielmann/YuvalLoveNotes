package subtext.yuvallovenotes.sendlettersreminder

import java.util.*

private interface AlarmSetter {

    /**
     * Method for preparing and saving data for an alarm required by the client.
     * Returns The calendar with the next alarm to be triggered.
     */
    fun prepare(): Calendar

}
