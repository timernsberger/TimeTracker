import React from 'react';
import { shallow } from 'enzyme';
import TaskHistory from '../../src/history/TaskHistory.jsx';
import ErrorsStore from '../../src/stores/ErrorsStore.js';

describe('TaskHistory', () => {
    it('renders without any history from the server', () => {
        fetch.mockResponse(JSON.stringify([]));
        const component = shallow(<TaskHistory.wrappedComponent errorsStore={ new ErrorsStore() } />);

        expect(component.exists()).toBe(true);
    });
});
