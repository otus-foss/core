"""Symbol definitions, one module per group. Each module has an ``ICONS`` dict of drawable name
(without ``symbol_``) to a function that returns an ``Icon``."""

import importlib
import pathlib

HERE = pathlib.Path(__file__).parent


def load(modules=()):
    """The icons of the named modules, or of every module when none are named."""
    names = modules or sorted(p.stem for p in HERE.glob("[!_]*.py"))
    return {
        name: make
        for module in names
        for name, make in importlib.import_module(f"glyphs.{module}").ICONS.items()
    }
