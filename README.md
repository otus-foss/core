![Otus](.github/banner.png)

Otus Core holds the Kotlin Multiplatform libraries that [Otus Mail](https://github.com/otus-foss/mail)
and [Otus Calendar](https://github.com/otus-foss/calendar) share: the JMAP client, contacts, push,
the design system, and suite services such as dates and avatars. It also holds the suite's product
and design documents and the architecture decisions for every repository.

> [!WARNING]
> Otus Core is still in development. Expect missing features and APIs that change between
> commits.

## Install

Core is not an app, and there is nothing to install. Each app builds Core from a checkout next to
its own, so clone all three side by side:

```sh
git clone https://github.com/otus-foss/core
git clone https://github.com/otus-foss/mail
git clone https://github.com/otus-foss/calendar
```

See [Using Core from an app](docs/consuming.md) for other layouts.

## Contributing

Read the [documentation](docs/README.md) and [AGENTS.md](AGENTS.md) before you change code.

## License

GPL-3.0-or-later, with an [additional permission](LICENSE-EXCEPTION.md) that allows app-store
distribution while the source stays public. See [LICENSE](LICENSE).
