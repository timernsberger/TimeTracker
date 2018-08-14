import React from 'react';
import { shallow } from 'enzyme';
import TaskSummary from '../../src/history/TaskSummary.jsx';

describe('TaskSummary', () => {
    it('renders', () => {
        const component = shallow(<TaskSummary tasks={[]} />);

        expect(component.exists()).toBe(true);
    });
});

