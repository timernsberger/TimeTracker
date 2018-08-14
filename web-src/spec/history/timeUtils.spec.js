import moment from 'moment';
import TimeUtils, { calculateDuration, subdivideDays } from '../../src/history/timeUtils.js';

describe('calculateDuration', () => {
    it('returns 0 for identical start and end times', () => {
        const epochSecond = moment().unix();
        const duration = calculateDuration({ timeStarted: { epochSecond }, timeEnded: { epochSecond } });
        expect(duration.asMilliseconds()).toBe(0);
    });

    it('diffs against current time if no end time is specified', () => {
        const now = moment();
        const expectedDuration = moment.duration({ hours: 2, minutes: 3 });
        const startSecond = now.clone().subtract(expectedDuration).unix();

        const stubbedMoment = () => now;
        stubbedMoment.unix = moment.unix;
        stubbedMoment.duration = moment.duration;
        TimeUtils.__Rewire__('moment', stubbedMoment); // replace with stubbed version

        let actualDuration;
        try {
            actualDuration = calculateDuration({ timeStarted: { epochSecond: startSecond }, timeEnded: null });
        } finally {
            TimeUtils.__ResetDependency__('moment');
        }

        expect(expectedDuration.asSeconds()).toBe(actualDuration.asSeconds());
    })
});


describe('subdivideDays', () => {
    it('generates the correct number of moments', () => {
        const fourDays = subdivideDays(0, 4);
        expect(fourDays.length).toBe(4);
        
        const noDays = subdivideDays(0, 0);
        expect(noDays.length).toBe(0);
        
        const twoDays = subdivideDays(2, 2);
        expect(twoDays.length).toBe(2);
    });

    it('lists days from most recent to oldest', () => {
        const days = subdivideDays(0, 4);
        days.reduce((previous, current) => {
            expect(previous.isBefore(current)).toBe(true);
            return current;
        });
    });

    it('returns midnight for all days', () => {
        const days = subdivideDays(2, 3);
        days.forEach((day) => {
            expect(day.hour()).toBe(0);
            expect(day.minute()).toBe(0);
        });
    });
});
