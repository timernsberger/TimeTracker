import React from 'react';
import moment from 'moment';
import SwipeContainer from 'react-swipe-events';
import { ToggleButtonGroup, ToggleButton } from 'react-bootstrap';
import { inject } from 'mobx-react';
import TaskChart from './TaskChart.jsx';
import TaskList from './TaskList.jsx';
import TaskSummary from './TaskSummary.jsx';

@inject("errorsStore")
class TaskHistory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: true,
            tasks: [],
            dateOffset: props.dateOffset || 0,
            numDays: 7
        };
    }

    render() {
        return (
            <div className="task-history">
                <ToggleButtonGroup name="daterange" defaultValue={7} onChange={this.onDateRangeChanged.bind(this)}>
                    <ToggleButton value={7} disabled={this.state.loading}>1 week</ToggleButton>
                    <ToggleButton value={14} disabled={this.state.loading}>2 weeks</ToggleButton>
                    <ToggleButton value={28} disabled={this.state.loading}>4 weeks</ToggleButton>
                    <ToggleButton value={26 * 7} disabled={this.state.loading}>half year</ToggleButton>
                    <ToggleButton value={365} disabled={this.state.loading}>1 year</ToggleButton>
                </ToggleButtonGroup>
                <SwipeContainer onSwipedLeft={() => this.onSwipe(-1)} onSwipedRight={() => this.onSwipe(1)}>
                    <TaskChart tasks={this.state.tasks} dateOffset={this.state.dateOffset} numDays={this.state.numDays} />
                </SwipeContainer>
                <TaskSummary tasks={this.state.tasks} />
                <TaskList tasks={this.state.tasks} />
                {this.state.loading && <div className="loading-spinner"><span className="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span></div>}
            </div>
        );
    }

    componentDidMount() {
        this.refreshData(this.state.numDays, this.state.dateOffset);
    }

    refreshData(numDays, dateOffset) {
        this.setState({ loading: true });
        let time = moment().endOf('day').unix();
        const startTime = time - 60 * 60 * 24 * (numDays + dateOffset);
        const endTime = time - 60 * 60 * 24 * (dateOffset);
        fetch(`/api/task/history?startTime=${startTime}&endTime=${endTime}`, {
            credentials: 'same-origin'
        }).then(
            (response) => response.json()
        ).then((tasks) => {
            this.setState({ tasks: tasks.filter(task => task.name !== 'None').reverse(), loading: false });
        }).catch((error) => {
            console.error(error);
            this.props.errorsStore.addError(`Something went wrong when loading your task history from ${numDays + dateOffset} to ${dateOffset} days ago.` +
                'Try refreshing the page or choosing a different time range.');
        });
    }

    onSwipe(direction) {
        const newOffset = Math.max(0, this.state.dateOffset + (this.state.numDays * Math.sign(direction)));
        this.setState({ dateOffset: newOffset });
        this.refreshData(this.state.numDays, newOffset);
    }

    onDateRangeChanged(numDays) {
        if(numDays < this.state.numDays) {
            const cutoff = moment().startOf('day').subtract(numDays + this.state.dateOffset, 'day').unix();
            const trimmedTasks = this.state.tasks.filter((task) => task.timeStarted.epochSecond >= cutoff);
            this.setState({ numDays, tasks: trimmedTasks });
        } else {
            this.setState({ numDays, tasks: [] });
            this.refreshData(numDays, this.state.dateOffset);
        }
    }
}

export default TaskHistory;