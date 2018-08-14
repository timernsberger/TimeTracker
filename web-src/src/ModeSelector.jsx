import React from 'react';
import { ToggleButtonGroup, ToggleButton } from 'react-bootstrap';
import AppMode from './AppMode.js';

const modeSelectorLabels = new Map([[AppMode.History, 'History'], [AppMode.TypeManager, 'Task Types']]);

export default function ModeSelector(props) {
    return (
        <div>
            <ToggleButtonGroup name="mode" defaultValue={props.mode} onChange={props.onModeChanged}>
                {
                    AppMode.enums.map((enumValue) => <ToggleButton key={enumValue} value={enumValue}>{modeSelectorLabels.get(enumValue)}</ToggleButton>)
                }
            </ToggleButtonGroup>
        </div>
    );
}