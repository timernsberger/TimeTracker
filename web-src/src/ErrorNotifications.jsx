import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { Alert } from 'react-bootstrap';

function ErrorNotification({ error, onDismiss }) {
    return (
        <Alert bsStyle="danger" onDismiss={onDismiss}>
            {error}
        </Alert>
    );
}

@inject("errorsStore")
@observer
class ErrorNotifications extends Component {
    render() {
        const errorsStore = this.props.errorsStore;
        return (
            <div>
                {
                    errorsStore.errors.map((error) => <ErrorNotification key={error} error={error} onDismiss={() => errorsStore.removeError(error)} />)
                }
            </div>
        );
    }
}

export default ErrorNotifications;