import re
from enum import Enum, auto
from pathlib import Path
from textwrap import dedent
from typing import Literal

import yaml
from pydantic import BaseModel

DEFAULT_OPTION_RGX = re.compile(r'(.*?)( \(.*\))?')


class OptType(Enum):
    single = auto()
    list = auto()
    dict = auto()


class ConfigOption(BaseModel):
    type: str | None = None
    kotlin_type: str | None = None
    required: Literal['true'] | None = None
    default: str | list | dict | None = None
    example: str | None = None
    description: str = ''


class ConfigSpec(BaseModel):
    title: str
    type: str
    default: str
    required: str
    example: str
    options: dict[str, ConfigOption]


def load_yml(path: Path):
    return yaml.load(path.read_text(), Loader=yaml.BaseLoader)


CODEGEN_DIR_PATH = Path(__file__).absolute().parent
REPO_ROOT = CODEGEN_DIR_PATH.parent

EN_CONFIG_PATH = CODEGEN_DIR_PATH / 'config' / 'en.yml'
RU_CONFIG_PATH = CODEGEN_DIR_PATH / 'config' / 'ru.yml'

KOTLIN_SCHEMA_PATH = (
    REPO_ROOT / 'common/src/main/kotlin/dev/vanutp/tgbridge/common/models/Config.kt'
)
DOCS_EN_PATH = REPO_ROOT / 'docs/en/reference.md'
DOCS_RU_PATH = REPO_ROOT / 'docs/ru/reference.md'

en_config = ConfigSpec.model_validate(load_yml(EN_CONFIG_PATH))
ru_config = ConfigSpec.model_validate(load_yml(RU_CONFIG_PATH))

if list(en_config.options.keys()) != list(ru_config.options.keys()):
    # TODO: check if required, type, example and default match
    raise ValueError('en and ru config keys don\'t match')


class KotlinField(BaseModel):
    name: str
    type: str | None = None
    opt_type: OptType | None = None
    comments: list[str] = []
    default: str | list | dict | None = None


class KotlinDataclass(BaseModel):
    name: str
    fields: list[KotlinField]


class DocsSection(BaseModel):
    name: str
    children: dict[str, 'DocsSection | ConfigOption']


def parse_name(name: str) -> tuple[str, OptType]:
    if name.endswith('[]'):
        return name.removesuffix('[]'), OptType.list
    elif name.endswith('{}'):
        return name.removesuffix('{}'), OptType.dict
    else:
        return name, OptType.single


def parse_default(default: str) -> tuple[str, str]:
    m = DEFAULT_OPTION_RGX.fullmatch(default)
    if not m:
        raise ValueError(f'Couldn\'t parse default option value {default}')
    return m.group(1), (m.group(2) or '')


def capitalize_first(s: str) -> str:
    return s[0].upper() + s[1:]


class KotlinRenderer:
    classes: dict[str, KotlinDataclass]
    spec: ConfigSpec

    def __init__(self, spec: ConfigSpec):
        self.classes = {}
        self.spec = spec

    def get_or_create_kotlin_cls(self, path: list[str]) -> KotlinDataclass:
        cls_key = '.'.join(path)
        if cls := self.classes.get(cls_key):
            return cls

        # TODO: validate if every class instance's opt_type is the same
        clean_path = []
        for p in path:
            p, typ = parse_name(p)
            if typ != OptType.single:
                p = p.removesuffix('s')
            clean_path.append(p)
        cls = KotlinDataclass(
            name=''.join(capitalize_first(x) for x in clean_path) + 'Config',
            fields=[],
        )

        if path:
            parent = self.get_or_create_kotlin_cls(path[:-1])
            comments = []
            if path == ['general']:
                # Comment at the top of the config
                comments = [
                    'It\'s enough to set botToken and chatId for the plugin to work.',
                    'When your group has topics enabled, you should also set topicId.',
                    'See https://tgbridge.vanutp.dev for more information.',
                ]
            opt_name, opt_type = parse_name(path[-1])
            parent_field = next((f for f in parent.fields if f.name == opt_name), None)
            if not parent_field:
                parent_field = KotlinField(name=opt_name)
                parent.fields.append(parent_field)
            if opt_type == OptType.single:
                parent_field.type = cls.name
            elif opt_type == OptType.list:
                parent_field.type = f'List<{cls.name}>'
            elif opt_type == OptType.dict:
                parent_field.type = f'Map<String, {cls.name}>'
            else:
                raise ValueError
            parent_field.comments.extend(comments)
            parent_field.opt_type = opt_type
            if parent_field.default:
                if opt_type == OptType.single:
                    raise ValueError(
                        'Default value for option groups can only be set for list/dict option groups'
                    )
                elif opt_type == OptType.list:
                    instances = []
                    for instance in parent_field.default:
                        vals = [f'{k} = {v}' for k, v in instance.items()]
                        instances.append(f'{cls.name}({', '.join(vals)})')
                    parent_field.default = f'listOf({', '.join(instances)})'
                elif opt_type == OptType.dict:
                    instances = []
                    for key, instance in parent_field.default.items():
                        vals = [f'{k} = {v}' for k, v in instance.items()]
                        instances.append(f'"{key}" to {cls.name}({', '.join(vals)})')
                    parent_field.default = f'mapOf({', '.join(instances)})'
            elif opt_type == OptType.list:
                parent_field.default = 'listOf()'
            elif opt_type == OptType.dict:
                parent_field.default = 'mapOf()'
            else:
                parent_field.default = f'{cls.name}()'

        self.classes[cls_key] = cls
        return cls

    @staticmethod
    def type_to_kotlin(schema_type: str) -> str:
        nullable_suffix = ' | null'
        nullable_mark = '?' if schema_type.endswith(nullable_suffix) else ''
        schema_type = schema_type.removesuffix(nullable_suffix)
        kotlin_base_type = {
            'string': 'String',
            'number': 'Int',
            'boolean': 'Boolean',
        }[schema_type]
        return kotlin_base_type + nullable_mark

    @staticmethod
    def render_field(field: KotlinField) -> list[str]:
        if isinstance(field.default, str):
            default = f' = {field.default}'
        elif field.default is None:
            default = ''
        else:
            raise ValueError(
                f'Invalid default value for field {field.name}: {field.default}'
            )
        if not field.type:
            raise ValueError(f'Field {field.name} has no type')
        rend_field = [f'val {field.name}: {field.type}{default},\n']
        if field.comments:
            rend_lines = []
            for line in field.comments:
                escaped_line = line.replace('\\', '\\\\').replace('"', '\\"')
                rend_lines.append(f'"{escaped_line}",\n')
            rend_comment = [
                '@YamlComment(\n',
                *['    ' + x for x in rend_lines],
                ')\n',
            ]
            rend_field = rend_comment + rend_field
        return rend_field

    def render_class(self, cls: KotlinDataclass) -> str:
        rend_field_lines = []
        for field in cls.fields:
            rend_field_lines.extend(self.render_field(field))
        return (
            '@Serializable\n'
            + f'data class {cls.name}(\n'
            + ''.join('    ' + x for x in rend_field_lines)
            + ')\n'
        )

    def render(self) -> str:
        res = (
            dedent(
                '''
                        // Generated by codegen/generate_config.py. Do not modify
                        package dev.vanutp.tgbridge.common.models
                
                        import com.charleskorn.kaml.YamlComment
                        import kotlinx.serialization.Serializable
                    '''
            ).strip()
            + '\n'
        )
        for k, opt in self.spec.options.items():
            *path, opt_name = k.split('.')
            opt_name, opt_type = parse_name(opt_name)

            type_ = opt.kotlin_type
            if not type_ and opt.type:
                type_ = self.type_to_kotlin(opt.type)

            comments = [x for x in opt.description.splitlines() if x]

            if opt.example:
                example_val, example_help = parse_default(opt.example)
                comments.append(f'Example: {example_val}{example_help}')

            if isinstance(opt.default, str):
                default_val, default_help = parse_default(opt.default)
                if default_help:
                    comments.append(f'Default value: {default_val}{default_help}')
            elif opt.default is None:
                default_val = None
            else:
                default_val = opt.default

            field = KotlinField(
                name=opt_name,
                type=type_,
                comments=comments,
                default=default_val,
                opt_type=opt_type,
            )

            cls = self.get_or_create_kotlin_cls(path)
            cls.fields.append(field)

        for cls in self.classes.values():
            res += '\n' + self.render_class(cls)

        return res


class DocsRenderer:
    root: DocsSection
    spec: ConfigSpec

    def __init__(self, spec: ConfigSpec):
        self.root = DocsSection(name=spec.title, children={})
        self.spec = spec

    def get_or_create_parent(self, path: list[str]) -> DocsSection:
        if not path:
            return self.root
        parent = self.get_or_create_parent(path[:-1])
        sect_name = path[-1]

        if sect_name in parent.children and isinstance(
            parent.children[sect_name], DocsSection
        ):
            return parent.children[sect_name]

        sect = DocsSection(
            name=sect_name,
            children={},
        )
        parent.children[sect_name] = sect
        return sect

    def render_opt(self, name: str, opt: ConfigOption, path: list[str]) -> str:
        depth = len(path) + 1
        res = '#' * depth + f' {name}\n\n'
        res += f'- **{self.spec.type}:** `{opt.type}`\n'
        if opt.required:
            res += f'- **{self.spec.required}**\n'
        if isinstance(opt.default, str):
            default_val, default_help = parse_default(opt.default)
        else:
            default_val = None
            default_help = ''
        hide_default_section = path[0] in ('general', 'events', 'version')
        if not opt.required and not hide_default_section or default_help:
            res += f'- **{self.spec.default}:** `{default_val}`{default_help}\n'
        if opt.example:
            example_val, example_help = parse_default(opt.example)
            res += f'- **{self.spec.example}:** `{example_val}`{example_help}\n'
        if opt.description:
            res += f'\n{opt.description}\n'
        return res

    def render_section(self, section: DocsSection, path: list[str]) -> str:
        depth = len(path) + 1
        res = '#' * depth + f' {section.name}\n'
        for name, child in section.children.items():
            res += '\n'
            if isinstance(child, DocsSection):
                res += self.render_section(child, path + [name])
            else:
                res += self.render_opt(name, child, path + [name])
        return res

    def render(self) -> str:
        res = '<!-- Generated by codegen/generate_config.py. Do not modify-->\n'
        for name, opt in self.spec.options.items():
            *path, opt_name = name.split('.')
            parent = self.get_or_create_parent(path)
            parent.children[opt_name] = opt
        res += self.render_section(self.root, [])
        return res


KOTLIN_SCHEMA_PATH.write_text(KotlinRenderer(en_config).render())
DOCS_EN_PATH.write_text(DocsRenderer(en_config).render())
DOCS_RU_PATH.write_text(DocsRenderer(ru_config).render())
