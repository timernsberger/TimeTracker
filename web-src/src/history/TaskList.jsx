import React from 'react';
import { calculateDuration } from './timeUtils.js';

function formatDateTime(date) {
    return date.toLocaleDateString() + " " + date.toLocaleTimeString();
}

function TimeEndedLabel({ timeEnded }) {
    if(timeEnded) {
        return <span>{formatDateTime(new Date(timeEnded.epochSecond * 1000))}</span>
    }
    return <span>now</span>;
}

function TaskDurationLabel({ task }) {
    const duration = calculateDuration(task);
    return (
        <span>{duration.hours()} hours, {duration.minutes()} minutes</span>
    );
}

function TaskHistoryEntry({ task }) {
    return (
        <div>
            <h3>{task.name} ({task.category})</h3>
            <div>
                <span>{formatDateTime(new Date(task.timeStarted.epochSecond * 1000))}</span>&nbsp;–&nbsp;
          <TimeEndedLabel timeEnded={task.timeEnded} />
                &nbsp;(<TaskDurationLabel task={task} />)
        </div>
        </div>
    );
}

export default function TaskList(props) {
    return (
        <div>
            {props.tasks.map((task, index) => <TaskHistoryEntry key={index} task={task} />)}
        </div>
    );
}