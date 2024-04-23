/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.services.ConfigService;
import client.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class MainCtrlTest {

    private MainCtrl sut;
    private ConfigService configService;
    private EventService eventService;

    @BeforeEach
    public void setup() {
        // Mock the ConfigService dependency
        configService = mock(ConfigService.class);
        eventService = mock(EventService.class);

        // Initialize your System Under Test (sut) with the mock
        sut = new MainCtrl(configService, eventService);
    }

    @Test
    public void writeSomeTests() {
        // Example test case
        // Assuming there's a method in MainCtrl that uses configService
        // For example: sut.performAction();
        // Verify the interaction or assert the expected outcomes

        // Example:
        // when(configService.getConfigValue("key")).thenReturn("value");
        // assertEquals("value", sut.getConfigValueForKey("key"));
    }
}
