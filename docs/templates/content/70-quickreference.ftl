# Quick reference

## Quick Links
* [All Properties](../properties/all-properties/)
* [Exit Codes](../advanced/troubleshooting/exit-codes/)

## ${solution_name} Modes

${solution_name} provides the following modes to assist with learning, troubleshooting, and setup.

| Mode | Command line option | Alt. option | Description |
| ---- | ------------------- | ----------- | ----------- |
| Help | --help | -h | Provides basic help information (including how to get more detailed help). |
| Interactive | --interactive | -i | Guides you through configuring ${solution_name}. |
| Diagnostic | --diagnostic | -d | Creates a zip file of diagnostic information for support. |
| Air Gap Creation | --zip | -z | Creates an air gap zip that you can use with the detect.*.air.gap.path arguments for running ${solution_name} offline. Optionally you can follow --zip with a space and an argument (for example: --zip GRADLE) to customize the air gap zip. Possible values: ALL (produce a full air gap zip; the default), NONE, NUGET (include only the NUGET inspector), GRADLE (include only the GRADLE inspector), DOCKER (include only the Docker Inspector). |
