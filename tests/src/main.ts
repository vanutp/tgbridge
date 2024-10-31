import { delay } from '@std/async';
import { PROJECT_ROOT_DIR, TEST_MATRIX } from './const.ts'
import { Server } from './server.ts'
import { TESTS } from './spec.ts'
import { logNoNewline, makeServer } from './utils.ts'
import { AssertionError } from './errors.ts'
import { escape as escapeRegex } from '@std/regexp';

interface PassedTest {
  test: string;
  failed: false;
}

interface FailedTest {
  test: string;
  failed: true;
  err: Error;
}

type TestResult = PassedTest | FailedTest;

async function buildIfRequired() {
  if (Deno.args.includes('--no-build')) {
    return;
  }
  console.log('Building project...');
  const command = new Deno.Command('./gradlew', {
    args: ['build'],
    stdin: 'null',
    cwd: PROJECT_ROOT_DIR,
  });
  const child = command.spawn();
  const status = await child.status;
  if (!status.success) {
    throw new Error('Failed to build project');
  }
}

function formatStack(err: Error, indent: number) {
  const stackLines = err.stack?.split('\n') ?? []
  stackLines.shift()
  if (err instanceof AssertionError) {
    stackLines.shift()
  }
  return stackLines
    .map((line) => ' '.repeat(indent) + line.trim())
    .join('\n');
}

async function main() {
  await buildIfRequired();

  const testsToRun = Deno.args
    .filter((arg) => !arg.startsWith('--'))
    .map(arg => {
      if (arg.endsWith('*')) {
        return new RegExp('^' + escapeRegex(arg.slice(0, -1)) + '.*$')
      } else {
        return new RegExp(`^${arg}$`)
      }
    })

  const servers: Server[] = [];
  for (const project of TEST_MATRIX) {
    for (const version of project.versions) {
      const key = `${project.server}-${version}`;
      if (testsToRun.length > 0 && testsToRun.every(pat => !pat.test(key))) {
        continue;
      }
      servers.push(makeServer(project.server, version, project.project));
    }
  }

  const testResults: { [key: string]: TestResult[] } = {};

  for (const server of servers) {
    console.log('-'.repeat(80));
    console.log(`Running tests for ${server.key}`);
    testResults[server.key] = [];

    if (!(await server.isInstalled())) {
      console.log(`[${server.key}] Installing server...`);
    } else {
      console.log(`[${server.key}] Configuring server...`);
    }
    await server.initialize();
    console.log(`[${server.key}] Starting server...`);
    await server.start();
    console.log(`[${server.key}] Server ready!`);

    for (const testFn of TESTS) {
      await logNoNewline(`[${server.key}] Running test ${testFn.name}... `);
      await server.reset();
      await delay(50)
      try {
        const res = testFn(server);
        if (res instanceof Promise) {
          await res;
        }
        console.log('[PASS]');
        testResults[server.key].push({
          test: testFn.name,
          failed: false,
        });
      } catch (err_) {
        const err = err_ as Error
        console.log('[FAIL]');
        console.log(err.toString())
        console.log(formatStack(err, 2))
        testResults[server.key].push({
          test: testFn.name,
          failed: true,
          err: err,
        });
      }
    }

    console.log(`[${server.key}] Stopping server...`);
    await server.stop();

    const passedTestsCount = testResults[server.key].filter((res) => !res.failed).length;
    console.log(`[${server.key}] ${passedTestsCount}/${TESTS.length} tests passed`);
  }

  console.log('-'.repeat(80));
  console.log('Test results:');

  let totalPassed = 0
  for (const [key, results] of Object.entries(testResults)) {
    const passedTestsCount = results.filter((res) => !res.failed).length;
    totalPassed += passedTestsCount
    console.log(`[${key}] ${passedTestsCount}/${TESTS.length} tests passed`);
    const failedTests = results.filter((res) => res.failed)
    if (failedTests.length > 0) {
      console.log('  Failed tests:');
      for (const res of failedTests) {
        const stack = formatStack(res.err, 6)
        console.log(`  - ${res.test}: ${res.err.toString()}`);
        console.log(stack)
      }
    }
  }

  console.log();
  console.log(`Total ${totalPassed}/${servers.length * TESTS.length} tests passed`);
}

await main();
