export enum RbacActivity {
  READ_ALL_PARTICIPANTS = 'READ_ALL_PARTICIPANTS',
  READ_ONE_PARTICIPANT = 'READ_ONE_PARTICIPANT',
  CREATE_PARTICIPANT = 'CREATE_PARTICIPANT',
  EDIT_PARTICIPANT = 'EDIT_PARTICIPANT',
  DELETE_PARTICIPANT = 'DELETE_PARTICIPANT',

  READ_ALL_CLUBS = 'READ_ALL_CLUBS',
  READ_ONE_CLUB = 'READ_ONE_CLUB',
  CREATE_CLUB = 'CREATE_CLUB',
  EDIT_CLUB = 'EDIT_CLUB',
  DELETE_CLUB = 'DELETE_CLUB',


  READ_ALL_TOURNAMENTS = 'READ_ALL_TOURNAMENTS',
  READ_ONE_TOURNAMENT = 'READ_ONE_TOURNAMENT',
  CREATE_TOURNAMENT = 'CREATE_TOURNAMENT',
  EDIT_TOURNAMENT = 'EDIT_TOURNAMENT',
  DELETE_TOURNAMENT = 'DELETE_TOURNAMENT',

  READ_ALL_ROLES = 'READ_ALL_ROLES',
  READ_ONE_ROLE = 'READ_ONE_ROLE',
  CREATE_ROLE = 'CREATE_ROLE',
  EDIT_ROLE = 'EDIT_ROLE',
  DELETE_ROLE = 'DELETE_ROLE',

  READ_ALL_TEAMS = 'READ_ALL_TEAMS',
  READ_ONE_TEAM = 'READ_ONE_TEAM',
  CREATE_TEAM = 'CREATE_TEAM',
  EDIT_TEAM = 'EDIT_TEAM',
  DELETE_TEAM = 'DELETE_TEAM',

  READ_ALL_FIGHTS = 'READ_ALL_FIGHTS',
  READ_ONE_FIGHT = 'READ_ONE_FIGHT',
  CREATE_FIGHT = 'CREATE_FIGHT',
  EDIT_FIGHT = 'EDIT_FIGHT',
  DELETE_FIGHT = 'DELETE_FIGHT',

  READ_ALL_DUELS = 'READ_ALL_DUELS',
  READ_ONE_DUEL = 'READ_ONE_DUEL',
  CREATE_DUEL = 'CREATE_DUEL',
  EDIT_DUEL = 'EDIT_DUEL',
  DELETE_DUEL = 'DELETE_DUEL',

  READ_ALL_GROUPS = 'READ_ALL_GROUPS',
  READ_ONE_GROUP = 'READ_ONE_GROUP',
  CREATE_GROUP = 'CREATE_GROUP',
  EDIT_GROUP = 'EDIT_GROUP',
  DELETE_GROUP = 'DELETE_GROUP',

  READ_ALL_RANKINGS = 'READ_ALL_RANKINGS',
  READ_ONE_RANKING = 'READ_ONE_RANKING',

  READ_ALL_USERS = 'READ_ALL_USERS',
  READ_ONE_USER = 'READ_ONE_USER',
  CREATE_USER = 'CREATE_USER',
  EDIT_USER = 'EDIT_USER',
  DELETE_USER = 'DELETE_USER',

}

export namespace RbacActivity {
  export function getByKey(key: string): RbacActivity | undefined {
    for (const valueKey in RbacActivity) {
      if ((RbacActivity as any)[valueKey] === key) {
        return <RbacActivity>valueKey;
      }
    }
    return undefined;
  }
}

export namespace RbacActivity {
  export function getKeys(): string[] {
    return Object.keys(RbacActivity).filter(enumValue => (typeof (RbacActivity[enumValue as RbacActivity]) !== 'function'))
  }
}

export namespace RbacActivity {
  export function toArray(): RbacActivity[] {
    return RbacActivity.getKeys().map(key => {
      return <RbacActivity>(<any>RbacActivity)[key];
    });
  }
}
