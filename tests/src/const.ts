import { ensureDirSync } from '@std/fs';

export const PROJECT_ROOT_DIR = new URL('../../', import.meta.url);
export const RUN_DIR = new URL('../run/', import.meta.url);
ensureDirSync(RUN_DIR);
export const RELEASES_DIR = new URL('build/release/', PROJECT_ROOT_DIR);
export const RELEASE_VER = Deno.readTextFileSync(new URL('gradle.properties', PROJECT_ROOT_DIR))
  .match(/projectVersion=(.*)/)![1]

export enum ServerType {
  fabric = 'fabric',
  forge = 'forge',
  neoforge = 'neoforge',
  paper = 'paper',
}

export const MAX_HEAP_SIZE = 1024 * 1024 * 2048

interface TestMatrixProject {
  project: string;
  server: ServerType;
  versions: string[];
}

// TODO: add 1.21.3
export const TEST_MATRIX: TestMatrixProject[] = [
  {
    project: 'fabric',
    server: ServerType.fabric,
    versions: ['1.19.2', '1.19.4', '1.20.1', '1.21.1', '1.21.4'],
  },
  {
    project: 'forge-1.19.2',
    server: ServerType.forge,
    versions: ['1.19.2'],
  },
  {
    project: 'forge-1.20.1',
    server: ServerType.forge,
    versions: ['1.20.1'],
  },
  {
    project: 'neoforge-1.21',
    server: ServerType.neoforge,
    versions: ['1.21.1', '1.21.4'],
  },
  {
    project: 'paper',
    server: ServerType.paper,
    versions: ['1.19.2', '1.21.4'],
  },
];
