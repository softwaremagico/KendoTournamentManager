import {Component, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
@Component({
  selector: 'redirect-guard',
  standalone: true,
  templateUrl: './redirect-guard.html'
})
export class RedirectGuard implements CanActivate {
  constructor(private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    window.location.href = '/';
    window.open(route.data['externalUrl'], '_blank');
    return true;
  }
}
