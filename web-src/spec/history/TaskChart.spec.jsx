import React from 'react';
import { shallow } from 'enzyme';
import TaskChart from '../../src/history/TaskChart.jsx';

describe('TaskChart', () => {
    it('renders', () => {
        const component = shallow(<TaskChart tasks={[]} numDays={0} />);

        expect(component.exists()).toBe(true);
    });
});

