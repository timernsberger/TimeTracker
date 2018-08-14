import React, { Component } from 'react';
import QueryString from 'querystring';
import { observer, Provider } from 'mobx-react';
import './App.scss';
import TaskTypesStore from './stores/TaskTypesStore';
import ErrorsStore from './stores/ErrorsStore';
import AppMode from './AppMode';
import ErrorNotifications from './ErrorNotifications';
import CurrentTask from './CurrentTask';
import TaskHistory from './history/TaskHistory';
import TaskTypeManager from './TaskTypeManager';
import ModeSelector from './ModeSelector';

@observer
class App extends Component {
    constructor() {
        super();

        const dateOffset = QueryString.parse(window.location.search.replace('?', ''))['dateOffset'];
        this.dateOffsetInt = dateOffset ? parseInt(dateOffset, 10) : 0;

        this.state = {
            mode: AppMode.History,
            taskStore: new TaskTypesStore(),
            errorsStore: new ErrorsStore(),
            loading: true
        };
        this.onModeChanged = this.onModeChanged.bind(this);
    }

    render() {
        if (this.state.loading) {
            return <div>loading...</div>;
        }

        let modeComponent;
        if (this.state.taskStore.taskTypes.length === 0 || this.state.mode === AppMode.TypeManager) {
            modeComponent = <TaskTypeManager />
        } else {
            modeComponent = <TaskHistory dateOffset={this.dateOffsetInt} />
        }

        return (
            <Provider taskStore={this.state.taskStore} errorsStore={this.state.errorsStore}>
                <div className="app">
                    <ErrorNotifications />
                    <div className="top-bar">
                        <CurrentTask />
                        <ModeSelector mode={this.state.mode} onModeChanged={this.onModeChanged} />
                    </div>
                    {modeComponent}
                </div>
            </Provider>
        );
    }

    componentDidMount() {
        fetch('/api/task/type/', {
            credentials: 'same-origin'
        }).then(
            (response) => response.json()
        ).then((taskTypes) => {
            this.setState({ loading: false });
            this.state.taskStore.taskTypes.replace(taskTypes);
        }).catch((error) => {
            this.state.errorsStore.addError('Something went wrong when loading your task types.');
            console.error(error);
        });
    }

    onModeChanged(mode) {
        this.setState({ mode });
    }
}

export default App;
