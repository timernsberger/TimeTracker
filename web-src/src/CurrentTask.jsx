import React from 'react';
import { FormControl } from 'react-bootstrap';
import { toJS } from 'mobx';
import { observer, inject } from 'mobx-react';

const defaultTaskType = { category: 'None', name: 'None' };

let none;

// Takes a list of task objects and flattens the task names and categories into a list for use in the current task dropdown.
// input type: Object[]; element structure = { category: String, name: String }
// output type: Object[]; element structure = { name: String, isCategory: Boolean }
function buildDropdownItems(taskTypes) {
    const categories = new Set();
    const distinctTasks = new Set();
    const items = [];
    taskTypes.forEach((task) => {
        const { category } = task;
        if (!categories.has(category)) {
            categories.add(category);
            items.push({
                category: category,
                isCategory: true
            });
        }
        // Set's equality operation is too strict for our mix of objects from task types, 
        // current task, and default, but their JSON representations are both unique and directly comparable
        let taskKey = JSON.stringify(toJS(task));
        if (!distinctTasks.has(taskKey)) {
            distinctTasks.add(taskKey);
            items.push({
                task: task,
                isCategory: false
            })
        }
    });
    return items;
}

function TaskSelectOption(props) {
    const item = props.item;
    if (item.isCategory) {
        return <option value={item.category} disabled className="category">{item.category}</option>
    } else {
        return <option value={JSON.stringify(item.task)} className="task">{'  ' + item.task.name}</option>
    }
}

@inject("taskStore", "errorsStore")
@observer
class CurrentTask extends React.Component {

    constructor() {
        super();
        this.state = {};
    }

    render() {
        if(this.state.fetchCurrentFailed) {
            return null;
        } else if (!this.state.task) {
            return <div>loading...</div>;
        }

        const allTasks = this.props.taskStore.taskTypes;

        const currentTaskType = { category: this.state.task.category, name: this.state.task.name };
        const dropdownItems = buildDropdownItems(allTasks.concat(currentTaskType, Object.assign({}, defaultTaskType)));

        return (
            <div className="current-task">
                <div>Current task: {this.state.task.name}</div>
                <FormControl componentClass="select" onChange={(e) => this.onChange(e.target.value)} value={JSON.stringify(this.state.task)} >
                    {
                        dropdownItems.map((item) => <TaskSelectOption key={ JSON.stringify(item) } item={item} />)
                    }
                </FormControl>
            </div>
        );
    }

    onChange(taskJson) {
        const task = JSON.parse(taskJson);
        fetch('/api/task/current', {
            headers: {
                'content-type': 'application/json'
            },
            body: JSON.stringify(task),
            credentials: 'same-origin',
            method: 'PUT'
        }).then(
            (response) => response.json()
        ).then((success) => {
            if (success) {
                this.setState({ task });
            } else {
                console.info('current task update failed or was rejected (server returned false)');
                this.props.errorsStore.addError("Sorry, we weren't able to update your current task. Please refresh the page and try again.");
            }
        }).catch((error) => {
            console.error(error);
            this.props.errorsStore.addError('Something went wrong when trying to update your current task.');
        });
    }

    componentDidMount() {
        fetch('/api/task/current', {
            credentials: 'same-origin'
        }).then((response) => {
            if (response.status === 204) {
                return Object.assign({}, defaultTaskType);
            } else {
                return response.json();
            }
        }).then((task) => {
            this.setState({ task });
        }).catch((error) => {
            console.error(error);
            this.props.errorsStore.addError("Something went wrong when trying get your current task from the server." + 
                " Don't worry - you'll still accumulate time on your current task, but you should refresh the page before doing anything else.");
            this.setState({ fetchCurrentFailed: true });
        });
    }
}

export default CurrentTask;