import React from 'react';
import { shallow } from 'enzyme';
import ErrorNotifications from '../src/ErrorNotifications.jsx';
import ErrorsStore from '../src/stores/ErrorsStore.js';

describe('ErrorNotifications', () => {
    it('renders', () => {
        const component = shallow(<ErrorNotifications.wrappedComponent errorsStore={ new ErrorsStore() } />);
        expect(component.exists()).toBe(true);
    });
});