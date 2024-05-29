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
package server;

import com.google.inject.Inject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import server.services.RandomGeneratorService;

@SpringBootApplication
@EntityScan(basePackages = {"commons", "server"})
public class Main {
    private final RandomGeneratorService randomGeneratorService;

    @Inject
    public Main(RandomGeneratorService randomGeneratorService) {
        this.randomGeneratorService = randomGeneratorService;
    }

    /**
     * @param args args
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        Main main = context.getBean(Main.class);
        main.passwordToConsole();
    }

    /**
     * Print the admin password to the console
     */
    public void passwordToConsole() {
        System.out.println("\033[0;1m" + "\u001B[36m" + "Admin password: " +
            randomGeneratorService.generatePassword() + "\033[0m");
    }
}
