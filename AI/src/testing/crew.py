from crewai import Agent, Crew, Process, Task
from crewai.project import CrewBase, agent, crew, task

from langchain_community.tools import YahooFinanceNewsTool

# Uncomment the following line to use an example of a custom tool
# from testing.tools.custom_tool import MyCustomTool

# Check our tools documentations for more information on how to use them
# from crewai_tools import SerperDevTool


@CrewBase
class TestingCrew:
    """Testing crew"""

    @agent
    def researcher(self) -> Agent:
        return Agent(
            config=self.agents_config["researcher"],
            llm="ollama/llama3.2",
            tools=[YahooFinanceNewsTool()],
            verbose=True,
        )

    @agent
    def accountant(self) -> Agent:
        return Agent(
            config=self.agents_config["accountant"],
            llm="ollama/llama3.2",
            verbose=True,
        )

    @agent
    def recommender(self) -> Agent:
        return Agent(
            config=self.agents_config["recommender"],
            llm="ollama/llama3.2",
            verbose=True,
        )

    @agent
    def blogger(self) -> Agent:
        return Agent(
            config=self.agents_config["blogger"],
            llm="ollama/llama3.2",
            verbose=True,
        )

    @task
    def research_task(self) -> Task:
        return Task(
            config=self.tasks_config["research_task"],
        )

    @task
    def accounting_task(self) -> Task:
        return Task(
            config=self.tasks_config["accounting_task"],
        )

    @task
    def recommending_task(self) -> Task:
        return Task(
            config=self.tasks_config["recommending_task"],
        )

    @task
    def reporting_task(self) -> Task:
        return Task(config=self.tasks_config["reporting_task"], output_file="report.md")

    @crew
    def crew(self) -> Crew:
        """Creates the Testing crew"""
        return Crew(
            agents=self.agents,  # Automatically created by the @agent decorator
            tasks=self.tasks,  # Automatically created by the @task decorator
            process=Process.sequential,
            verbose=True,
            # process=Process.hierarchical, # In case you wanna use that instead https://docs.crewai.com/how-to/Hierarchical/
        )
