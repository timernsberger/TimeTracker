import React from 'react';
import moment from 'moment';
import { Bar } from 'react-chartjs-2';
import { calculateDuration, subdivideDays } from './timeUtils.js';
import colorGenerator from './colorGenerator.js';

function groupTasks(tasks, days) {
    const timeByTask = {};

    tasks.forEach((task) => {
        const duration = calculateDuration(task);
        // TODO: handle tasks that cross days
        const startDay = moment(task.timeStarted.epochSecond * 1000).startOf('day');
        const index = days.findIndex((day) => day.isSame(startDay));
        if(index >= 0) {
            timeByTask[task.name] = timeByTask[task.name] || [];
            timeByTask[task.name][index] = (timeByTask[task.name][index] || 0) + duration.hours() * 60 + duration.minutes();
        }
    });

    Object.values(timeByTask).forEach((eventsByDay) => {
        for(let i = 0; i < days.length; i++) {
            eventsByDay[i] = eventsByDay[i] || [];
        }
    });

    return timeByTask;
}

const baseDataset = {
    borderWidth: 1
};

export default function TaskChart(props) {
    const dayOffset = props.dateOffset || 0;
    const days = subdivideDays(dayOffset, props.numDays);
    const timeByTask = groupTasks(props.tasks, days);

    const colors = colorGenerator();

    const datasets = Object.entries(timeByTask).map(([name, days]) => {
        return Object.assign({
            label: name,
            data: days
        }, colors.next().value, baseDataset);
    });

    const labels = days.map(d => d.format("DD MMM"));
    const data = {
        labels,
        datasets
    };

    const options = {
        scales: {
            yAxes: [{
                ticks: {
                    min: 0
                }
            }]
        }
    };

    return (
        <Bar data={data} options={options} />
    )
}