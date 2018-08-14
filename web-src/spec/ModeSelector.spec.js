import React from 'react';
import { mount } from 'enzyme';
import AppMode from '../src/AppMode.js';
import ModeSelector from '../src/ModeSelector.jsx';

describe('ModeSelector', () => {
    it('highlights the button matching the current mode', () => {
        const component = mount(<ModeSelector mode={ AppMode.TypeManager } />);

        const activeButton = component.find('.btn.active');
        expect(activeButton.text()).toBe('Task Types');
    });

    it('lists all options', () => {
        const component = mount(<ModeSelector mode={ AppMode.TypeManager } />);

        expect(component.find('.btn').length).toBe(AppMode.enums.length);
    })
});