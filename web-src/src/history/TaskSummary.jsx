import React from 'react';
import { Doughnut } from 'react-chartjs-2';
import moment from 'moment';
import colorGenerator from './colorGenerator.js';

function formatTimeSpent(timeSpent) {
    const totalMinutes = Math.floor(timeSpent / 60);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    let minuteText;
    if(minutes > 1) {
        minuteText = minutes.toFixed() + ' minutes';
    } else {
        minuteText = minutes.toFixed() + ' minute';
    }
    if(hours >= 2) {
        return hours.toFixed() + ' hours, ' + minuteText;
    } else if(hours >= 1) {
        return hours.toFixed() + ' hour, ' + minuteText;
    } else {
        return minuteText;
    }
}

export default function TaskSummary(props) {
    const colors = colorGenerator();
    const taskTotals = {};
    props.tasks.forEach((task) => {
        const key = task.name;
        const timeSpent = (task.timeEnded ? task.timeEnded.epochSecond : moment().unix()) - task.timeStarted.epochSecond;
        taskTotals[key] = (taskTotals[key] || 0) + timeSpent;
    });
    const labels = [];
    const data = [];
    const backgroundColor = [];
    Object.entries(taskTotals).forEach(([name, total]) => {
        labels.push(`${name} (${formatTimeSpent(total)})`);
        data.push(total / 60);
        backgroundColor.push(colors.next().value.backgroundColor);
    });
    const datasets = [{
        data,
        backgroundColor
    }];

    return (
        <div>
            <Doughnut data={{ labels, datasets }} />
        </div>
    );
}