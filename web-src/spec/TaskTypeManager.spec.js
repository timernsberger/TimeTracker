import React from 'react';
import { shallow } from 'enzyme';
import TaskTypeManager from '../src/TaskTypeManager.jsx';
import TaskTypesStore from '../src/stores/TaskTypesStore.js';
import ErrorsStore from '../src/stores/ErrorsStore.js';

describe('TaskTypeManager', () => {
    it('renders', () => {
        const component = shallow(<TaskTypeManager.wrappedComponent 
            taskStore={ new TaskTypesStore() }
            errorsStore={ new ErrorsStore() } />);
            
        expect(component.exists()).toBe(true);
    });
});