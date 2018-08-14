import React, { Component } from 'react';
import { FormControl, FormGroup, ControlLabel } from 'react-bootstrap';
import { observer, inject } from 'mobx-react';

function TaskType({ taskType, onRemoved }) {
    return (
        <div className="task-type">
            <span className="task-name">{ taskType.name }</span>
            <span className="glyphicon glyphicon-remove task-remove-button" onClick={ () => onRemoved(taskType) }></span>
        </div>
    );
}

function TaskCategory({ categoryName, taskTypes, onTaskTypeRemoved }) {
    return (
        <div className="task-category">
            <h3>{ categoryName }</h3>
            <div className="tasks">
                {
                    taskTypes.map((taskType) => <TaskType key={ taskType.name } taskType={ taskType } onRemoved={ onTaskTypeRemoved } />)
                }
            </div>
        </div>
    );
};

@inject("taskStore")
@observer
class TaskCreator extends Component {
    constructor() {
        super();

        this.state = {
            category: '',
            name: ''
        };
    }

    render() {
        return (
            <div>
                <FormGroup controlId="category">
                    <ControlLabel>Category</ControlLabel>
                    <FormControl type="text" value={ this.state.category } onChange={ (e) => this.setState({ category: e.target.value }) } />
                </FormGroup>
                <FormGroup controlId="name">
                    <ControlLabel>Name</ControlLabel>
                    <FormControl type="text" value={ this.state.name } onChange={ (e) => this.setState({ name: e.target.value }) } />
                </FormGroup>
                <div><button className="btn btn-success" onClick={ this.submit.bind(this) } disabled={ !this.areInputsValid() }>Add</button></div>
            </div>
        );
    }

    areInputsValid() {
        return this.state.category.trim().length > 0 &&
            this.state.name.trim().length > 0 &&
            !this.props.taskStore.taskTypes.find((task) => {
                return (task.name === this.state.name) && 
                    (task.category === this.state.category)
            });
    }

    submit() {
        if(this.areInputsValid()) {
            this.props.onTaskTypeCreated({
                category: this.state.category,
                name: this.state.name
            }, 
            () => this.setState({ name: '' }));
        }
    }
}

@inject("taskStore", "errorsStore")
@observer
class TaskTypeManager extends Component {
    constructor() {
        super();
        this.onTaskTypeRemoved = this.onTaskTypeRemoved.bind(this); //blech
        this.onTaskTypeCreated = this.onTaskTypeCreated.bind(this);
    }

    render() {
        const taskCategories = {};
        this.props.taskStore.taskTypes.forEach((taskType) => {
            taskCategories[taskType.category] = taskCategories[taskType.category] || { categoryName: taskType.category, taskTypes: [] };
            taskCategories[taskType.category].taskTypes.push(taskType);
        });

        return (
            <div>
                <h2>Task Types</h2>
                <div className="task-categories">
                    {
                        Object.values(taskCategories).map((taskCategory) => {
                            return <TaskCategory
                                key={ taskCategory.categoryName }
                                categoryName={ taskCategory.categoryName }
                                taskTypes={ taskCategory.taskTypes }
                                onTaskTypeRemoved={ this.onTaskTypeRemoved } />
                        })
                    }
                </div>
                <TaskCreator onTaskTypeCreated={ this.onTaskTypeCreated } />
            </div>
        );
    }

    onTaskTypeRemoved(task) {
        fetch('/api/task/type/', {
            credentials: 'same-origin',
            method: 'DELETE',
            body: JSON.stringify(task),
            headers: {
                'content-type': 'application/json'
            }
        }).then(() => {
            this.props.taskStore.taskTypes.remove(task);
        }).catch((error) => {
            console.error(error);
            this.props.errorsStore.addError(`Something went wrong when trying to remove "${task.category} - ${task.name}". Try removing it again and/or refreshing the page.`);
        });
    }

    onTaskTypeCreated(task, successCallback) {
        fetch('/api/task/type/', {
            credentials: 'same-origin',
            method: 'PUT',
            body: JSON.stringify(task),
            headers: {
                'content-type': 'application/json'
            }
        }).then(() => {
            this.props.taskStore.taskTypes.push(task);
            successCallback();
        }).catch((error) => {
            console.error(error);
            this.props.errorsStore.addError('Something went wrong when trying to add your new task type. Please try again. You may need to refresh the page.');
        });
    }
}

export default TaskTypeManager;