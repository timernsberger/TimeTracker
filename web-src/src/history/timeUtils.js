import moment from 'moment';

/**
 * Produces a momentjs Duration between a start time and an end time. Uses the current time if no end time is specified.
 * This is primarily meant to convert data from the server, which specifies times as Java 8 Instant objects.
 * Input structure:
 * {
 *   timeStarted: Instant, (required)
 *   timeEnded: Instant? (optional)
 * }
 * Instant structure:
 * {
 *   epochSecond: number
 * }
 */
export function calculateDuration({ timeStarted, timeEnded }) {
    const momentStarted = moment.unix(timeStarted.epochSecond);
    let momentEnded;
    if(timeEnded) {
        momentEnded = moment.unix(timeEnded.epochSecond);
    } else { // default to now if no end
        momentEnded = moment();
        momentEnded.milliseconds(0); // clear ms since server data only goes down to seconds
    }
    return moment.duration(momentEnded.diff(momentStarted));
}

/**
 * Generates an array of moments from midnight dayOffset days ago going back to dayOffset + numDays ago
 * @param {number} dayOffset - how many days ago from today to start
 * @param {number} numDays - how many days to include prior to dayOffset
 */
export function subdivideDays(dayOffset, numDays) {
    const days = [];
    for(let i = dayOffset; i < numDays + dayOffset; i++) {
        days.unshift(moment().startOf('day').subtract(i, 'day'));
    }
    return days;
}
