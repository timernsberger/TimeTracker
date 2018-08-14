import React from 'react';
import { shallow } from 'enzyme';
import { toJS } from 'mobx';
import App from '../src/App.jsx';

describe('App', () => {
    it('renders', () => {
        fetch.mockResponse(JSON.stringify([]));
        const component = shallow(<App />);

        expect(component.exists()).toBe(true);
    });

    it('requests task types from server and puts results in store', () => {
        const taskTypes = [
            { category: 'Test', name: 'a' },
            { category: 'Test', name: 'Test' },
            { category: 'Other', name: 'blah' }
        ];
        fetch.mockResponse(JSON.stringify(taskTypes));
        const component = shallow(<App />);

        setImmediate(() => {
            component.update();

            const taskStore = component.state('taskStore');
            expect(taskStore.taskTypes.length).toBe(3);
            expect(toJS(taskStore.taskTypes)).toContainEqual(taskTypes[0]);
        });
    });
});